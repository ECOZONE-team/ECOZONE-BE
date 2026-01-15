package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.PaymentCommandLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 지급 명령(Idempotency) 로그 리포지토리
 * - idemKey 기준 조회/저장
 * - Spring Data JPA 기반
 * @since 2025-09-16
 */
public interface PaymentCommandLogRepository extends JpaRepository<PaymentCommandLogJpaEntity, Long> {
    Optional<PaymentCommandLogJpaEntity> findByIdemKey(String idemKey);
    Optional<PaymentCommandLogJpaEntity> findByOrderId(String orderId);
}
