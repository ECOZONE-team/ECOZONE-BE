package co.ecozone.ecozoneapi.payment.infrastructure.persistence;

import co.ecozone.ecozoneapi.payment.application.port.out.PaymentCommandRepository;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentCommand;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.PaymentCommandLogJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * 결제 명령 저장소 어댑터
 * - PaymentCommand 도메인 모델 ↔ PaymentCommandLogJpaEntity 변환
 * @since 2025-01-15
 */
@Component
@RequiredArgsConstructor
public class PaymentCommandRepositoryAdapter implements PaymentCommandRepository {

    private final PaymentCommandLogRepository jpaRepository;
    private final Clock clock;

    @Override
    public PaymentCommand save(PaymentCommand command) {
        if (command.getId() == null) {
            // INSERT
            var entity = PaymentCommandLogJpaEntity.from(command);
            var saved = jpaRepository.saveAndFlush(entity);
            return saved.toDomain();
        } else {
            // UPDATE
            var managed = jpaRepository.findById(command.getId())
                    .orElseThrow(() -> new IllegalStateException("PaymentCommand not found: " + command.getId()));
            managed.applyFrom(command, Instant.now(clock));
            jpaRepository.save(managed);
            return managed.toDomain();
        }
    }

    @Override
    public Optional<PaymentCommand> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdemKey(idempotencyKey)
                .map(PaymentCommandLogJpaEntity::toDomain);
    }

    @Override
    public Optional<PaymentCommand> findById(Long id) {
        return jpaRepository.findById(id)
                .map(PaymentCommandLogJpaEntity::toDomain);
    }

    @Override
    public Optional<PaymentCommand> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId)
                .map(PaymentCommandLogJpaEntity::toDomain);
    }
}
