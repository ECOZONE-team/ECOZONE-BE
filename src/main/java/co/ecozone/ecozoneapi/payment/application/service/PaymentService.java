package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.payment.application.dto.ConfirmResult;
import co.ecozone.ecozoneapi.payment.application.port.in.CancelPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.in.ConfirmPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentProvider;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.PaymentCommandLogJpaEntity;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.PaymentCommandLogRepository;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.PaymentRepositoryAdapter;
import co.ecozone.ecozoneapi.platform.web.error.ApiException;
import co.ecozone.ecozoneapi.platform.web.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import static co.ecozone.ecozoneapi.payment.infrastructure.persistence.PaymentCommandLogJpaEntity.Status.*;

/**
 * 결제 애플리케이션 서비스
 * - 2-Phase 흐름: (1) 예약/락 → (2) PG 호출 → (3) 결과 반영
 * - Idempotency-Key를 활용한 중복 승인 방지 및 재시도 안정성 제공
 * - 짧은 트랜잭션(조회→변경 적용) 원칙 준수, @Version 기반 낙관적 잠금 보조
 * - 포트(UseCase/Provider/Repository)만 의존하며, 프레임워크 의존 최소화
 * @since 2025-09-16
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService implements ConfirmPaymentUseCase, CancelPaymentUseCase {

    private final PaymentRepositoryAdapter paymentRepo;      // lockByOrderId 사용
    private final PaymentCommandLogRepository cmdLogRepo;
    private final PaymentProvider provider;
    private final TransactionTemplate tx;
    private final Clock clock;


    /** 1) 예약(AUTHORIZED까지) — 짧은 TX */
    private Long reserveAuthorize(String orderId, long amount, String paymentKey) {
        return tx.execute(status -> {
            var current = paymentRepo.lockByOrderId(orderId).orElse(null);

            Payment p = (current == null)
                    ? Payment.initiated(orderId, amount)
                    : current;

            // 이미 확정/취소면 idemponent 처리
            switch (p.getStatus()) {
                case CONFIRMED -> { return p.getId(); }     // OK: 재시도 성공 처리
                case CANCELED  -> { throw new IllegalStateException("already canceled"); }
                default -> { /* 진행 */ }
            }
            // paymentKey 채워 AUTHORIZED 상태로 전이
            var saved = paymentRepo.save(p.authorized(paymentKey));
            return saved.getId();
        });
    }

    /** 2) 외부(PG) 호출 — 트랜잭션 없음 */
    private ConfirmResult callProviderConfirm(String paymentKey, String orderId, long amount) {
        return provider.confirm(paymentKey, orderId, amount);
    }

    /** 3) 결과 반영 — 짧은 TX (REQUIRES_NEW) */
    private void applyConfirmResult(Long paymentId, ConfirmResult res) {
        tx.executeWithoutResult(s -> {
            var p = paymentRepo.findById(paymentId).orElseThrow();
            if (res.ok()) paymentRepo.save(p.confirmed(res.approvedAt()));
            else          paymentRepo.save(p.failed(res.failureReason()));
        });
    }

    @Override
    public Long cancel(Long paymentId, String reason) {
        // 취소 역시 2-Phase: 상태 확인/락 → PG 취소 → 반영
        var payment = tx.execute(st -> paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("payment not found")));

        if (payment.getStatus() != PaymentStatus.CONFIRMED)
            throw new IllegalStateException("only confirmed payment can be canceled");

        var res = provider.cancel(payment.getPaymentKey(), reason);

        tx.executeWithoutResult(st -> {
            var p = paymentRepo.findById(paymentId).orElseThrow();
            if (res.ok()) paymentRepo.save(p.canceled(res.canceledAt(), reason));
            else          paymentRepo.save(p.failed(res.failureReason()));
        });
        return paymentId;
    }

    @Override
    public Long confirm(String idemKey, String paymentKey, String orderId, long amount) {
        if (idemKey == null || idemKey.isBlank())
            throw new ApiException(ErrorCode.BAD_REQUEST, "Idempotency-Key required");

        // A-1) idemKey 선점 or 재사용
        var now = Instant.now(clock);
        PaymentCommandLogJpaEntity log = tx.execute(status -> cmdLogRepo.findByIdemKey(idemKey)
                .map(existing -> {
                    return switch (existing.getStatus()) {
                        case SUCCEEDED -> existing; // 바로 재사용
                        case IN_PROGRESS -> throw new ApiException(ErrorCode.CONFLICT, "processing");
                        case FAILED -> throw new ApiException(ErrorCode.CONFLICT, "previously failed");
                    };
                })
                .orElseGet(() -> {
                    // 유니크 인덱스 통해 선점 시도
                    var started = PaymentCommandLogJpaEntity.start(
                            idemKey, orderId, hash(paymentKey, orderId, amount), now);
                    try {
                        return cmdLogRepo.saveAndFlush(started);
                    } catch (DataIntegrityViolationException dup) {
                        // 동시에 선점된 경우 → 재조회하여 상태에 따라 응답
                        var again = cmdLogRepo.findByIdemKey(idemKey).orElseThrow();
                        switch (again.getStatus()) {
                            case SUCCEEDED -> { return again; }
                            case IN_PROGRESS -> throw new ApiException(ErrorCode.CONFLICT, "processing");
                            case FAILED -> throw new ApiException(ErrorCode.CONFLICT, "previously failed");
                        }
                        throw new IllegalStateException("unreachable");
                    }
                })
        );

        if (log.getStatus() == SUCCEEDED) {
            return Objects.requireNonNull(log.getPaymentId(), "paymentId");
        }

        // A-2) Payment 예약(authorize) — 짧은 TX
        Long paymentId = tx.execute(st -> {
            var current = paymentRepo.lockByOrderId(orderId).orElse(null);
            Payment p = (current == null) ? Payment.initiated(orderId, amount) : current;

            switch (p.getStatus()) {
                case CONFIRMED -> { return p.getId(); }  // 이미 확정: idem 처리
                case CANCELED  -> throw new ApiException(ErrorCode.CONFLICT, "already canceled");
                default -> {} // 진행
            }
            var saved = paymentRepo.save(p.authorized(paymentKey));
            return saved.getId();
        });

        // PG 호출 — 트랜잭션 밖
        var res = provider.confirm(paymentKey, orderId, amount);

        // B) 결과 반영 — 짧은 TX
        tx.executeWithoutResult(st -> {
            var p = paymentRepo.findById(paymentId).orElseThrow();
            if (res.ok()) {
                paymentRepo.save(p.confirmed(res.approvedAt()));
                var managed = cmdLogRepo.findById(log.getId()).orElseThrow();
                managed.markSucceeded(paymentId, Instant.now(clock));
            } else {
                paymentRepo.save(p.failed(res.failureReason()));
                var managed = cmdLogRepo.findById(log.getId()).orElseThrow();
                managed.markFailed(res.failureReason(), Instant.now(clock));
            }
        });

        return paymentId;
    }

    private static String hash(String paymentKey, String orderId, long amount) {
        var src = paymentKey + "|" + orderId + "|" + amount;
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(src.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(src.hashCode());
        }
    }
}
