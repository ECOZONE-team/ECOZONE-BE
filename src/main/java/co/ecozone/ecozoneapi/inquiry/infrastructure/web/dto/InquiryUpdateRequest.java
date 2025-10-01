package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record InquiryUpdateRequest(
        @NotBlank String name,
        @NotBlank String phone,
        String note
) {}