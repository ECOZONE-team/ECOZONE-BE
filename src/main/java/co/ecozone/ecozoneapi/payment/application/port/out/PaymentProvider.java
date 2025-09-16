package co.ecozone.ecozoneapi.payment.application.port.out;



import co.ecozone.ecozoneapi.payment.application.dto.CancelResult;
import co.ecozone.ecozoneapi.payment.application.dto.ConfirmResult;

import java.time.Instant;

/**
 * PG 연동 포트(출력 포트)
 * - confirm/cancel 등 외부 결제사 호출 계약 정의
 * - 애플리케이션은 이 포트에만 의존 (구현 교체 용이)
 * @since 2025-09-16
 */
public interface PaymentProvider {

    /** 클라이언트가 받은 paymentKey + orderId + amount 로 서버 승인(confirm) */
    ConfirmResult confirm(String paymentKey, String orderId, long amount);

    /** 서버 취소 */
    CancelResult cancel(String paymentKey, String reason);
}