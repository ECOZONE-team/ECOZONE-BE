package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import co.ecozone.ecozoneapi.payment.application.dto.ConfirmResult;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentCommandRepository;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.LedgerEntry;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentCommand;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import co.ecozone.ecozoneapi.payment.infrastructure.PaymentProperties;
import co.ecozone.ecozoneapi.platform.web.error.ApiException;
import co.ecozone.ecozoneapi.platform.web.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * 결제 명령 실행기
 * - 공통 트랜잭션 로직 추출
 * - REQUIRES_NEW 트랜잭션 관리
 * - 멱등성 보장 로직
 * @since 2025-01-15
 */
@Component
@RequiredArgsConstructor
public class PaymentCommandExecutor {

    private final PaymentCommandRepository commandRepository;
    private final PaymentRepository paymentRepository;
    private final PlatformTransactionManager txm;
    private final PaymentProperties paymentProperties;
    private final Clock clock;

    private TransactionTemplate txNew() {
        TransactionTemplate t = new TransactionTemplate(txm);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        t.setTimeout(30);
        return t;
    }

    /**
     * 멱등키 선점 (REQUIRES_NEW)
     */
    public PaymentCommand reserveIdempotencyKey(String idemKey, String orderId, String paymentKey, long amount) {
        return txNew().execute(status -> {
            Optional<PaymentCommand> existing = commandRepository.findByIdempotencyKey(idemKey);
            if (existing.isPresent()) {
                PaymentCommand cmd = existing.get();
                if (cmd.isSucceeded()) {
                    return cmd;
                }
                if (cmd.isInProgress()) {
                    throw new ApiException(ErrorCode.CONFLICT, "processing");
                }
                throw new ApiException(ErrorCode.CONFLICT, "previously failed");
            }

            PaymentCommand newCmd = PaymentCommand.start(
                idemKey,
                orderId,
                hash(paymentKey, orderId, amount),
                Instant.now(clock)
            );
            return commandRepository.save(newCmd);
        });
    }

    /**
     * 결제 예약 (AUTHORIZED 상태로) (REQUIRES_NEW)
     */
    public Long reservePayment(String orderId, String paymentKey, long amount, UserId userId) {
        return txNew().execute(st -> {
            Payment current = paymentRepository.lockByOrderId(orderId).orElse(null);
            Payment p;

            if (current == null) {
                p = Payment.initiated(userId, orderId, amount, Instant.now(clock));
            } else {
                // 소유자 검증
                if (!Objects.equals(current.getUserId(), userId)) {
                    throw new ApiException(ErrorCode.AUTH_ACCESS_DENIED, "order owner mismatch");
                }
                // 금액 검증
                if (current.getAmount() != amount) {
                    throw new ApiException(ErrorCode.BAD_REQUEST, "amount mismatch");
                }
                // 이미 승인됨
                if (current.getStatus() == PaymentStatus.CONFIRMED) {
                    return current.getId();
                }
                // 취소됨
                if (current.getStatus() == PaymentStatus.CANCELED) {
                    throw new ApiException(ErrorCode.CONFLICT, "already canceled");
                }
                p = current;
            }

            return paymentRepository.save(p.authorized(paymentKey, Instant.now(clock))).getId();
        });
    }

    /**
     * Confirm 결과 반영 (REQUIRES_NEW)
     */
    public void applyConfirmResult(Long paymentId, Long commandId, ConfirmResult result) {
        txNew().executeWithoutResult(st -> {
            Payment payment = paymentRepository.findById(paymentId).orElseThrow();
            PaymentCommand command = commandRepository.findById(commandId).orElseThrow();

            // 이미 terminal 상태면 CommandLog만 업데이트
            if (payment.getStatus().isTerminal()) {
                if (command.isInProgress()) {
                    if (payment.getStatus() == PaymentStatus.CONFIRMED) {
                        commandRepository.save(command.markSucceeded(paymentId, Instant.now(clock)));
                    } else {
                        commandRepository.save(command.markFailed(
                            Optional.ofNullable(payment.getFailureReason()).orElse("terminal"),
                            Instant.now(clock)
                        ));
                    }
                }
                return;
            }

            if (result.ok()) {
                // 성공: 상태 변경 + 원장 기록
                paymentRepository.save(payment.confirmed(result.approvedAt(), Instant.now(clock)));
                paymentRepository.append(LedgerEntry.of(
                    payment.getOrderId(),
                    "USER:" + payment.getUserId(),
                    "MERCHANT:eco",
                    payment.getAmount(),
                    Instant.now(clock),
                    "confirm"
                ));
                commandRepository.save(command.markSucceeded(paymentId, Instant.now(clock)));
            } else {
                // 실패: 상태 변경 + CommandLog
                paymentRepository.save(payment.failed(normalizeFailure(result.failureReason()), Instant.now(clock)));
                commandRepository.save(command.markFailed(normalizeFailure(result.failureReason()), Instant.now(clock)));
                throw new ApiException(ErrorCode.PAYMENT_CONFIRM_FAILED, normalizeFailure(result.failureReason()));
            }
        });
    }

    /**
     * Cancel 결과 반영 (REQUIRES_NEW)
     */
    public void applyCancelResult(Long paymentId, Long commandId, boolean success, String reason) {
        txNew().executeWithoutResult(st -> {
            Payment payment = paymentRepository.findById(paymentId).orElseThrow();
            PaymentCommand command = commandRepository.findById(commandId).orElseThrow();

            // 이미 취소된 경우
            if (payment.getStatus() == PaymentStatus.CANCELED) {
                if (command.isInProgress()) {
                    commandRepository.save(command.markSucceeded(paymentId, Instant.now(clock)));
                }
                return;
            }

            if (success) {
                paymentRepository.save(payment.canceled(reason, Instant.now(clock)));

                PaymentProperties.Ledger ledgerConfig = paymentProperties.getLedger();
                paymentRepository.append(LedgerEntry.of(
                        payment.getOrderId(),
                        ledgerConfig.getUserAccountPrefix() + payment.getUserId(),
                        ledgerConfig.getMerchantAccountPrefix() + ledgerConfig.getMerchantId(),
                        payment.getAmount(),
                        Instant.now(clock),
                        ledgerConfig.getConfirmMemo()
                ));
                commandRepository.save(command.markSucceeded(paymentId, Instant.now(clock)));
            } else {
                commandRepository.save(command.markFailed(normalizeFailure(reason), Instant.now(clock)));
                throw new ApiException(ErrorCode.PAYMENT_CANCEL_FAILED, normalizeFailure(reason));
            }
        });
    }

    private static String normalizeFailure(String raw) {
        if (raw == null) return "unknown";
        // JSON 응답에서 code 추출
        int codeIdx = raw.indexOf("NOT_FOUND_PAYMENT_SESSION");
        if (codeIdx >= 0) return "NOT_FOUND_PAYMENT_SESSION";
        return raw.length() > 200 ? raw.substring(0, 200) : raw;
    }

    public static String hash(String paymentKey, String orderId, long amount) {
        var src = paymentKey + "|" + orderId + "|" + amount;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(src.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
