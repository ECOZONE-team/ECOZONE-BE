package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.payment.application.dto.CancelResult;
import co.ecozone.ecozoneapi.payment.application.port.in.CancelPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentProvider;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentCommand;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import co.ecozone.ecozoneapi.platform.web.error.ApiException;
import co.ecozone.ecozoneapi.platform.web.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/**
 * 결제 취소 서비스
 * - 2-Phase 멱등 취소 처리
 * - SRP: 단일 책임 (결제 취소만)
 * @since 2025-01-15
 */
@Service
@RequiredArgsConstructor
public class CancelPaymentService implements CancelPaymentUseCase {

    private final PaymentCommandExecutor commandExecutor;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PlatformTransactionManager txm;

    private TransactionTemplate txNew() {
        TransactionTemplate t = new TransactionTemplate(txm);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return t;
    }

    @Override
    public Long cancel(Long paymentId, String reason) {
        // 멱등키 자동 생성
        String idemKey = "cancel:" + paymentId;

        // A-1) 결제 정보 조회 (orderId 획득)
        Payment payment = txNew().execute(st ->
            paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "payment not found"))
        );

        // A-2) 멱등키 선점 (REQUIRES_NEW)
        PaymentCommand command = commandExecutor.reserveIdempotencyKey(
            idemKey,
            payment.getOrderId(),
            payment.getPaymentKey(),
            payment.getAmount()
        );

        // 이미 성공한 경우 즉시 반환
        if (command.isSucceeded()) {
            return Objects.requireNonNull(command.getPaymentId(), "paymentId");
        }

        // A-3) 결제 상태 확인 (REQUIRES_NEW)
        String paymentKey = txNew().execute(st -> {
            Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST, "payment not found"));

            // 이미 취소됨 - 멱등 처리
            if (p.getStatus() == PaymentStatus.CANCELED) {
                return null;
            }

            // CONFIRMED 상태만 취소 가능
            if (p.getStatus() != PaymentStatus.CONFIRMED) {
                throw new ApiException(ErrorCode.BAD_REQUEST, "only confirmed payment can be canceled");
            }

            return p.getPaymentKey();
        });

        // 이미 취소된 경우 즉시 반환
        if (paymentKey == null) {
            commandExecutor.applyCancelResult(paymentId, command.getId(), true, reason);
            return paymentId;
        }

        // B) PG 취소 호출 (트랜잭션 없음)
        CancelResult result;
        try {
            result = paymentProvider.cancel(paymentKey, reason);
        } catch (Exception e) {
            result = new CancelResult(false, null, "provider_error:" + e.getClass().getSimpleName());
        }

        // C) 결과 반영 (REQUIRES_NEW)
        commandExecutor.applyCancelResult(paymentId, command.getId(), result.ok(), reason);

        return paymentId;
    }
}
