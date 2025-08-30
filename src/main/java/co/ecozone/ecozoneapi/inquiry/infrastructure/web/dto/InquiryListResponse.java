package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

public record InquiryListResponse(
        Long id,
        String companyName,
        String name,
        java.time.Instant createdAt,
        boolean answered
) {}