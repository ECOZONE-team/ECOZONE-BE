package co.ecozone.ecozoneapi.payment.infrastructure.persistence;


import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.PaymentJpaEntity;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.SpringDataPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 결제 리포지토리 어댑터(JPA)
 * - 저장 전략: 신규 persist, 기존 find→applyFrom→flush (분리/버전 오류 방지)
 * - lockByOrderId 등 직렬화 보장을 위한 잠금 쿼리 제공
 * @since 2025-09-16
 */
@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository repo;

    @Override
    public Payment save(Payment p) {
        var saved = repo.save(PaymentJpaEntity.from(p));
        return saved.toDomain();
    }

    /** 확인 단계에서 경합 방지용 */
    public Optional<Payment> lockByOrderId(String orderId) {
        return repo.lockByOrderId(orderId).map(PaymentJpaEntity::toDomain);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return repo.findById(id).map(PaymentJpaEntity::toDomain);
    }
}