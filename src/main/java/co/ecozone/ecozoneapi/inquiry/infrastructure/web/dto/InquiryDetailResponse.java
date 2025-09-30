package co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto;
import java.time.Instant;

public record InquiryDetailResponse(
        Long id,
        Long companyIdx,
        String companyName,
        String name,
        String phone,
        String note,
        boolean answered,
        Instant createdAt
) {}