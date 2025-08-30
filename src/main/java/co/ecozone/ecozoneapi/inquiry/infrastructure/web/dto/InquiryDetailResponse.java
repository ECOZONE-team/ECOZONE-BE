package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

public record InquiryDetailResponse(
        Long id,
        Long companyIdx,
        String companyName,
        String name,
        String phone,
        String note,
        boolean answered,
        java.time.Instant createdAt
) {}