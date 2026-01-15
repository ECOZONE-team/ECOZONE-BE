package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.application.port.out.ReconciliationRepository;
import co.ecozone.ecozoneapi.payment.domain.model.ReconciliationEntry;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.ReconciliationEntryJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 조정 저장소 어댑터
 * @since 2025-01-15
 */
@Component
@RequiredArgsConstructor
public class ReconciliationRepositoryAdapter implements ReconciliationRepository {

    private final SpringDataReconciliationRepository repository;
    private final Clock clock;

    @Override
    public ReconciliationEntry save(ReconciliationEntry entry) {
        if (entry.getId() == null) {
            // INSERT
            var saved = repository.save(ReconciliationEntryJpaEntity.from(entry));
            return saved.toDomain();
        } else {
            // UPDATE
            var managed = repository.findById(entry.getId())
                    .orElseThrow(() -> new IllegalStateException("ReconciliationEntry not found: " + entry.getId()));
            managed.applyFrom(entry, Instant.now(clock));
            repository.save(managed);
            return managed.toDomain();
        }
    }

    @Override
    public List<ReconciliationEntry> findByBatchId(String batchId) {
        return repository.findByReconciliationBatchId(batchId).stream()
                .map(ReconciliationEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<ReconciliationEntry> findManualRequiredEntries() {
        return repository.findManualRequiredEntries().stream()
                .map(ReconciliationEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<ReconciliationEntry> findDiscrepancies() {
        return repository.findDiscrepancies().stream()
                .map(ReconciliationEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<ReconciliationEntry> findLatestByOrderId(String orderId) {
        return repository.findLatestByOrderId(orderId)
                .map(ReconciliationEntryJpaEntity::toDomain);
    }

    @Override
    public List<ReconciliationEntry> findByDateRange(Instant from, Instant to) {
        return repository.findByDateRange(from, to).stream()
                .map(ReconciliationEntryJpaEntity::toDomain)
                .toList();
    }
}
