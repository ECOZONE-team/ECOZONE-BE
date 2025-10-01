package co.ecozone.ecozoneapi.inquiry.application.dto;

import java.time.Instant;

public record InquiryDetail(
        Long id,
        Long companyIdx,
        String companyName,
        String name,
        String phone,
        String note,
        boolean answered,
        Instant createdAt
) {}