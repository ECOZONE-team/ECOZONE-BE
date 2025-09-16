package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.domain.model.Payment;

import java.util.Optional;

/**
 * 결제 리포지토리 포트(출력 포트)
 * - 결제 저장/조회/락 등 영속 연산 계약 정의
 * - 구현은 인프라 어댑터에서 맡음
 * @since 2025-09-16
 */
public interface PaymentRepository {
    Payment save(Payment p);
    Optional<Payment> lockByOrderId(String orderId);
    Optional<Payment> findById(Long id);
}