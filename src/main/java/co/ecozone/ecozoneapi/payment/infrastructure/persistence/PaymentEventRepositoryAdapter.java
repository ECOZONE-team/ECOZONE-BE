package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.application.port.out.PaymentEventRepository;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.PaymentEventJpaEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 결제 이벤트 저장소 어댑터
 * @since 2025-01-15
 */
@Component
@RequiredArgsConstructor
public class PaymentEventRepositoryAdapter implements PaymentEventRepository {

    private final SpringDataPaymentEventRepository repository;

    @Override
    public PaymentEvent save(PaymentEvent event) {
        if (event.getId() == null) {
            // INSERT
            var saved = repository.save(PaymentEventJpaEntity.from(event));
            return saved.toDomain();
        } else {
            // UPDATE
            var managed = repository.findById(event.getId())
                    .orElseThrow(() -> new IllegalStateException("PaymentEvent not found: " + event.getId()));
            managed.applyFrom(event);
            repository.save(managed);
            return managed.toDomain();
        }
    }

    @Override
    public Optional<PaymentEvent> findByEventId(String eventId) {
        return repository.findByEventId(eventId)
                .map(PaymentEventJpaEntity::toDomain);
    }

    @Override
    public List<PaymentEvent> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId).stream()
                .map(PaymentEventJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<PaymentEvent> findPendingEvents() {
        return repository.findPendingEvents().stream()
                .map(PaymentEventJpaEntity::toDomain)
                .toList();
    }
}
