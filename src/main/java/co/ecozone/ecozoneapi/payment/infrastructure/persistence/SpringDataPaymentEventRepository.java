package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.PaymentEventJpaEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 결제 이벤트 Spring Data JPA Repository
 * @since 2025-01-15
 */
public interface SpringDataPaymentEventRepository extends JpaRepository<PaymentEventJpaEntity, Long> {

    Optional<PaymentEventJpaEntity> findByEventId(String eventId);

    List<PaymentEventJpaEntity> findByOrderId(String orderId);

    /**
     * 미처리 이벤트 조회
     */
    @Query("SELECT e FROM PaymentEventJpaEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<PaymentEventJpaEntity> findPendingEvents();

}
