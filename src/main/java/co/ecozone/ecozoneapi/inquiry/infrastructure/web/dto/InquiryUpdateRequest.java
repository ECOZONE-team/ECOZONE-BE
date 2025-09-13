package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

public record InquiryUpdateRequest(
        String name,
        String phone,
        String note
) {}
