package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InquiryCreateRequest(
        @NotNull Long companyIdx,
        @NotBlank String companyName,
        @NotBlank String name,
        @NotBlank String phone,
        String note
) {}