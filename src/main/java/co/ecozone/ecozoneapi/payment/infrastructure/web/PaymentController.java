package co.ecozone.ecozoneapi.payment.infrastructure.web;

import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import co.ecozone.ecozoneapi.payment.application.port.in.CancelPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.in.ConfirmPaymentUseCase;
import co.ecozone.ecozoneapi.payment.application.port.in.CreateOrderUseCase;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.request.CancelRequest;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.request.ConfirmRequest;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.request.CreateOrderRequest;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.response.CancelResponse;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.response.ConfirmResponse;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.response.CreateOrderResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 웹 컨트롤러 (API)
 * - POST /api/payments/confirm: Idempotency-Key 필수, 승인 처리
 * - POST /api/payments/{id}/cancel: 승인된 결제 취소
 * - Web DTO ↔ UseCase 변환만 담당, 비즈니스 로직 없음
 * @since 2025-09-16
 */
@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {

    private final CreateOrderUseCase createUC;
    private final ConfirmPaymentUseCase confirmUC;
    private final CancelPaymentUseCase cancelUC;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> create(@RequestBody @Valid CreateOrderRequest req,
                                                      @AuthenticationPrincipal JwtPrincipal principal) {
        Long id = createUC.create(req.orderId(), req.amount(), principal.userId());
        return ResponseEntity.ok(new CreateOrderResponse(id));
    }

    /** 결제 승인 (클라이언트가 받은 paymentKey를 서버가 최종 승인) */
    // payment/infrastructure/web/PaymentController.java
    @PostMapping("/confirm")
    public ResponseEntity<ConfirmResponse> confirm(
            @RequestHeader("Idempotency-Key") String idemKey,
            @RequestBody @Valid ConfirmRequest req,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Long id = confirmUC.confirm(idemKey, principal.userId(), req.paymentKey(), req.orderId(), req.amount());
        return ResponseEntity.ok(new ConfirmResponse(id));
    }


    /** 결제 취소(상점) */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<CancelResponse> cancel(@PathVariable Long paymentId, @RequestBody CancelRequest req) {
        Long id = cancelUC.cancel(paymentId, req.reason());
        return ResponseEntity.ok(new CancelResponse(id));
    }

}
