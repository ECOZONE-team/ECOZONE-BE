package co.ecozone.ecozoneapi.payment.application.dto;

import co.ecozone.ecozoneapi.payment.application.port.out.CommandLogRepository;

import java.time.Instant;

public record CommandLog(Long id, String idemKey, String orderId, String hash, Status status,
                  Long paymentId, Instant startedAt, Instant endedAt) {}

