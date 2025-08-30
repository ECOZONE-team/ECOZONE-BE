package co.ecozone.ecozoneapi.inquiry.application.dto;

import java.time.Instant;

public record InquirySummary(
        Long id,
        String companyName,
        String name,
        Instant createdAt,
        boolean answered
) {}