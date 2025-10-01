package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;
import java.time.Instant;

public record InquiryListResponse(
        Long id,
        String companyName,
        String name,
        Instant createdAt,
        boolean answered
) {}