package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.auth.domain.model.UserId;
import co.ecozone.ecozoneapi.payment.application.port.in.CreateOrderUseCase;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * 주문 생성 서비스
 * - 결제 초기 상태(INITIATED) 생성
 * - SRP: 단일 책임 (주문 생성만)
 * @since 2025-01-15
 */
@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final PaymentRepository paymentRepository;
    private final Clock clock;

    @Override
    @Transactional
    public Long create(String orderId, long amount, UserId userId) {
        Instant now = Instant.now(clock);
        Payment payment = Payment.initiated(userId, orderId, amount, now);
        Payment saved = paymentRepository.save(payment);
        return saved.getId();
    }
}
