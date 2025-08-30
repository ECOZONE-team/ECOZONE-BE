package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

public record InquiryCreateRequest(
        Long companyIdx,
        String companyName,
        String name,
        String phone,
        String note
) {}