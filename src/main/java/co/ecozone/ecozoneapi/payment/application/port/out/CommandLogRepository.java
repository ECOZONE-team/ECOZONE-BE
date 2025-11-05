package co.ecozone.ecozoneapi.payment.application.port.out;

import co.ecozone.ecozoneapi.payment.application.dto.CommandLog;

import java.time.Instant;
import java.util.Optional;

public interface CommandLogRepository {
    Optional<CommandLog> findByIdemKey(String idemKey);
    CommandLog saveAndFlush(CommandLog started);
    void markSucceeded(Long id, Long paymentId, Instant endedAt);
    void markFailed(Long id, String reason, Instant endedAt);
}
