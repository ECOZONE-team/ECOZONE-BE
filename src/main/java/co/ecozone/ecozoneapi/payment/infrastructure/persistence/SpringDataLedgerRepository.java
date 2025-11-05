package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.LedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataLedgerRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {
    List<LedgerEntryJpaEntity> findByOrderId(String orderId);
}
