package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record InquiryUpdateRequest(
        @NotBlank(message = "name은 필수입니다.") String name,
        @NotBlank(message = "phone은 필수입니다.") String phone,
        String note
) {}
