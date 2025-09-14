package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InquiryCreateRequest(
        @NotNull(message = "companyIdx는 필수입니다.") Long companyIdx,
        @NotBlank(message = "companyName은 필수입니다.") String companyName,
        @NotBlank(message = "name은 필수입니다.") String name,
        @NotBlank(message = "phone은 필수입니다.") String phone,
        String note
) {}
