package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import co.ecozone.ecozoneapi.payment.application.dto.ConfirmResult;
import co.ecozone.ecozoneapi.payment.application.port.in.ConfirmPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentProvider;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentCommand;
import co.ecozone.ecozoneapi.platform.web.error.ApiException;
import co.ecozone.ecozoneapi.platform.web.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 결제 승인 서비스
 * - 2-Phase 멱등 승인 처리
 * - SRP: 단일 책임 (결제 승인만)
 * @since 2025-01-15
 */
@Service
@RequiredArgsConstructor
public class ConfirmPaymentService implements ConfirmPaymentUseCase {

    private final PaymentCommandExecutor commandExecutor;
    private final PaymentProvider paymentProvider;

    @Override
    public Long confirm(String idemKey, UserId userId, String paymentKey, String orderId, long amount) {
        // 입력 검증
        if (idemKey == null || idemKey.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Idempotency-Key required");
        }
        if (userId == null) {
            throw new ApiException(ErrorCode.AUTH_REQUIRED, "user required");
        }

        // A-1) 멱등키 선점 (REQUIRES_NEW)
        PaymentCommand command = commandExecutor.reserveIdempotencyKey(idemKey, orderId, paymentKey, amount);

        // 이미 성공한 경우 즉시 반환
        if (command.isSucceeded()) {
            return Objects.requireNonNull(command.getPaymentId(), "paymentId");
        }

        // A-2) 결제 예약 (AUTHORIZED 상태로) (REQUIRES_NEW)
        Long paymentId = commandExecutor.reservePayment(orderId, paymentKey, amount, userId);

        // B) PG 승인 호출 (트랜잭션 없음)
        ConfirmResult result;
        try {
            result = paymentProvider.confirm(paymentKey, orderId, amount);
        } catch (Exception e) {
            result = new ConfirmResult(false, null, "provider_error:" + e.getClass().getSimpleName());
        }

        // C) 결과 반영 (REQUIRES_NEW)
        commandExecutor.applyConfirmResult(paymentId, command.getId(), result);

        return paymentId;
    }
}
