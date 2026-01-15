package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import co.ecozone.ecozoneapi.payment.application.dto.CancelResult;
import co.ecozone.ecozoneapi.payment.application.dto.ConfirmResult;
import co.ecozone.ecozoneapi.payment.application.port.in.CancelPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.in.ConfirmPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.in.CreateOrderUseCase;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentProvider;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.LedgerEntry;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.PaymentCommandLogJpaEntity;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.PaymentCommandLogRepository;
import co.ecozone.ecozoneapi.platform.web.error.ApiException;
import co.ecozone.ecozoneapi.platform.web.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * 결제 애플리케이션 서비스 (DEPRECATED)
 * - 2-Phase 흐름: (1) 예약/락 → (2) PG 호출 → (3) 결과 반영
 * - Idempotency-Key를 활용한 중복 승인 방지 및 재시도 안정성 제공
 * - 짧은 트랜잭션(조회→변경 적용) 원칙 준수, @Version 기반 낙관적 잠금 보조
 * - 포트(UseCase/Provider/Repository)만 의존하며, 프레임워크 의존 최소화
 *
 * @deprecated SRP 위반으로 분리됨. 대신 다음을 사용:
 *   - {@link CreateOrderService}
 *   - {@link ConfirmPaymentService}
 *   - {@link CancelPaymentService}
 * @since 2025-09-16
 */
@Deprecated(since = "2025-01-15", forRemoval = true)
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("deprecation") // PaymentCommandLogJpaEntity.Status 레거시 호환성 유지
public class PaymentService implements CreateOrderUseCase, ConfirmPaymentUseCase, CancelPaymentUseCase {

    private final PaymentRepository paymentRepo;
    private final PaymentCommandLogRepository cmdLogRepo;
    private final PaymentProvider provider;
    private final PlatformTransactionManager txm;
    private final TransactionTemplate tx;
    private final Clock clock;

    private TransactionTemplate txNew() {
        TransactionTemplate t = new TransactionTemplate(txm);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return t;
    }

    @Override
    public Long create(String orderId, long amount, UserId userId) {
        var now = Instant.now(clock);
        Payment saved = tx.execute(st -> paymentRepo.save(Payment.initiated(userId, orderId, amount, now)));
        return saved.getId();
    }

    @Override
    public Long cancel(Long paymentId, String reason) {
        return cancel(null, paymentId, reason);
    }

    /**
     * 결제 취소 (멱등성 보장)
     * @param idemKey 멱등키 (선택 사항, 없으면 paymentId 기반 자동 생성)
     * @param paymentId 결제 ID
     * @param reason 취소 사유
     */
    public Long cancel(String idemKey, Long paymentId, String reason) {
        final Instant now = Instant.now(clock);

        // 멱등키 생성 (없으면 "cancel:{paymentId}" 사용)
        final String effectiveIdemKey = (idemKey != null && !idemKey.isBlank())
            ? idemKey
            : "cancel:" + paymentId;

        // A-1) 멱등키 선점 — REQUIRES_NEW
        PaymentCommandLogJpaEntity cmd = txNew().execute(status -> {
            return cmdLogRepo.findByIdemKey(effectiveIdemKey).map(existing -> {
                return switch (existing.getStatus()) {
                    case SUCCEEDED -> existing;
                    case IN_PROGRESS -> throw new ApiException(ErrorCode.CONFLICT, "cancel in progress");
                    case FAILED -> throw new ApiException(ErrorCode.CONFLICT, "cancel previously failed");
                };
            }).orElseGet(() -> {
                // 결제 정보 조회하여 orderId 획득
                Payment p = paymentRepo.findById(paymentId)
                    .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "payment not found"));
                return cmdLogRepo.saveAndFlush(
                    PaymentCommandLogJpaEntity.start(effectiveIdemKey, p.getOrderId(),
                        hash(p.getPaymentKey(), p.getOrderId(), p.getAmount()), now)
                );
            });
        });

        if (cmd.getStatus() == PaymentCommandLogJpaEntity.Status.SUCCEEDED) {
            return Objects.requireNonNull(cmd.getPaymentId(), "paymentId");
        }

        // A-2) 결제 상태 확인 및 락 — REQUIRES_NEW
        String paymentKey = txNew().execute(st -> {
            Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "payment not found"));

            // 상태 검증
            if (payment.getStatus() == PaymentStatus.CANCELED) {
                // 이미 취소됨 - 멱등 처리
                PaymentCommandLogJpaEntity log = cmdLogRepo.findById(cmd.getId()).orElseThrow();
                if (log.getStatus() == PaymentCommandLogJpaEntity.Status.IN_PROGRESS) {
                    log.markSucceeded(paymentId, now);
                    cmdLogRepo.save(log);
                }
                return null; // 이미 취소됨 표시
            }

            if (payment.getStatus() != PaymentStatus.CONFIRMED) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "only confirmed payment can be canceled");
            }

            return payment.getPaymentKey();
        });

        // 이미 취소된 경우 즉시 반환
        if (paymentKey == null) {
            return paymentId;
        }

        // B) PG 취소 호출 — 트랜잭션 없음
        CancelResult res;
        try {
            res = provider.cancel(paymentKey, reason);
        } catch (Exception e) {
            res = new CancelResult(false, null, "provider_error:" + e.getClass().getSimpleName());
        }

        // C) 결과 반영 (+원장) — REQUIRES_NEW
        final String[] failureHolder = new String[1];
        CancelResult finalRes = res;
        txNew().executeWithoutResult(st -> {
            Payment managed = paymentRepo.findById(paymentId).orElseThrow();
            PaymentCommandLogJpaEntity log = cmdLogRepo.findById(cmd.getId()).orElseThrow();

            // 이미 취소된 경우
            if (managed.getStatus() == PaymentStatus.CANCELED) {
                if (log.getStatus() == PaymentCommandLogJpaEntity.Status.IN_PROGRESS) {
                    log.markSucceeded(paymentId, now);
                }
                return;
            }

            if (finalRes.ok()) {
                paymentRepo.save(managed.canceled(reason, now));
                paymentRepo.append(LedgerEntry.of(
                    managed.getOrderId(),
                    "MERCHANT:eco",
                    "USER:" + managed.getUserId(),
                    managed.getAmount(),
                    now,
                    "cancel:" + reason
                ));
                log.markSucceeded(paymentId, now);
            } else {
                log.markFailed(normalizeFailure(finalRes.failureReason()), now);
                failureHolder[0] = normalizeFailure(finalRes.failureReason());
            }
        });

        // 트랜잭션 밖에서 예외 던지기
        if (failureHolder[0] != null) {
            throw new ApiException(ErrorCode.PAYMENT_CANCEL_FAILED, failureHolder[0]);
        }

        return paymentId;
    }

    public Long confirm(String idemKey, UserId userId, String paymentKey, String orderId, long amount) {
        if (idemKey == null || idemKey.isBlank()) throw new ApiException(ErrorCode.BAD_REQUEST, "Idempotency-Key required");
        if (userId == null) throw new ApiException(ErrorCode.AUTH_REQUIRED, "user required");

        final Instant now = Instant.now(clock);

        // A-1) 멱등키 선점 — REQUIRES_NEW (짧게)
        PaymentCommandLogJpaEntity cmd = txNew().execute(status -> {
            return cmdLogRepo.findByIdemKey(idemKey).map(existing -> {
                return switch (existing.getStatus()) {
                    case SUCCEEDED -> existing;
                    case IN_PROGRESS -> throw new ApiException(ErrorCode.CONFLICT, "processing");
                    case FAILED -> throw new ApiException(ErrorCode.CONFLICT, "previously failed");
                };
            }).orElseGet(() -> cmdLogRepo.saveAndFlush(
                    PaymentCommandLogJpaEntity.start(idemKey, orderId, hash(paymentKey, orderId, amount), now)
            ));
        });

        if (cmd.getStatus() == PaymentCommandLogJpaEntity.Status.SUCCEEDED) {
            return Objects.requireNonNull(cmd.getPaymentId(), "paymentId");
        }
        if (cmd.getStatus() == PaymentCommandLogJpaEntity.Status.FAILED) {
            throw new ApiException(ErrorCode.CONFLICT, "previously failed");
        }

        // A-2) 예약(AUTHORIZED) — REQUIRES_NEW
        Long paymentId = txNew().execute(st -> {
            Payment current = paymentRepo.lockByOrderId(orderId).orElse(null);
            Payment p;
            if (current == null) {
                p = Payment.initiated(userId, orderId, amount, now);
            } else {
                if (!Objects.equals(current.getUserId(), userId)) throw new ApiException(ErrorCode.AUTH_ACCESS_DENIED, "order owner mismatch");
                if (current.getAmount() != amount) throw new ApiException(ErrorCode.BAD_REQUEST, "amount mismatch");
                if (current.getStatus() == PaymentStatus.CONFIRMED) return current.getId();
                if (current.getStatus() == PaymentStatus.CANCELED)  throw new ApiException(ErrorCode.CONFLICT, "already canceled");
                p = current;
            }
            return paymentRepo.save(p.authorized(paymentKey, now)).getId();
        });

        // B) PG 승인 호출 — 트랜잭션 없음
        ConfirmResult res;
        try {
            res = provider.confirm(paymentKey, orderId, amount);
        } catch (Exception net) {
            res = new ConfirmResult(false, null, "provider_error:" + net.getClass().getSimpleName());
        }

        // C) 결과 반영(+원장)
        final String[] failureHolder = new String[1];
        ConfirmResult finalRes = res;
        txNew().executeWithoutResult(st -> {
            Payment managed = paymentRepo.findById(paymentId).orElseThrow();
            PaymentCommandLogJpaEntity log = cmdLogRepo.findById(cmd.getId()).orElseThrow();

            if (managed.getStatus().isTerminal()) {
                if (log.getStatus() == PaymentCommandLogJpaEntity.Status.IN_PROGRESS) {
                    if (managed.getStatus() == PaymentStatus.CONFIRMED) log.markSucceeded(paymentId, Instant.now(clock));
                    else log.markFailed(Optional.ofNullable(managed.getFailureReason()).orElse("terminal"), Instant.now(clock));
                }
                return;
            }

            if (finalRes.ok()) {
                paymentRepo.save(managed.confirmed(finalRes.approvedAt(), now));
                paymentRepo.append(LedgerEntry.of(
                        managed.getOrderId(),
                        "USER:" + managed.getUserId(),
                        "MERCHANT:eco",
                        managed.getAmount(),
                        now,
                        "confirm"
                ));
                log.markSucceeded(paymentId, Instant.now(clock));
            } else {
                // 실패는 저장만
                paymentRepo.save(managed.failed(normalizeFailure(finalRes.failureReason()), now));
                log.markFailed(normalizeFailure(finalRes.failureReason()), Instant.now(clock));
                failureHolder[0] = normalizeFailure(finalRes.failureReason());
            }
        });

        // 트랜잭션 밖에서 예외 변환
        if (failureHolder[0] != null) {
            String reason = failureHolder[0];
            if (reason.contains("NOT_FOUND_PAYMENT_SESSION")) {
                throw new ApiException(ErrorCode.PAYMENT_SESSION_NOT_FOUND, reason);
            }
            throw new ApiException(ErrorCode.PAYMENT_CONFIRM_FAILED, reason);
        }
        return paymentId;
    }

    private static String normalizeFailure(String raw) {
        // 토스 응답처럼 JSON 문자열이 그대로 들어오는 경우를 대비해 간단히 정제
        if (raw == null) return "unknown";
        // {"code":"NOT_FOUND_PAYMENT_SESSION","message":"..."} 형태면 code만 추출
        int codeIdx = raw.indexOf("NOT_FOUND_PAYMENT_SESSION");
        if (codeIdx >= 0) return "NOT_FOUND_PAYMENT_SESSION";
        return raw.length() > 200 ? raw.substring(0, 200) : raw;
    }


    private static String hash(String paymentKey, String orderId, long amount) {
        var src = paymentKey + "|" + orderId + "|" + amount;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(src.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(src.hashCode());
        }
    }
}
