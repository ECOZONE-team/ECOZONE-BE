package co.ecozone.ecozoneapi.inquiry.presentation.dto;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryDetailResponse {
    private Long idx;
    private String companyName;
    private String name;
    private String phone;
    private String note;
    private LocalDateTime regDate;

    public static InquiryDetailResponse from(Inquiry inquiry) {
        return InquiryDetailResponse.builder()
                .idx(inquiry.getIdx())
                .companyName(inquiry.getCompanyName())
                .name(inquiry.getName())
                .phone(inquiry.getPhone())
                .note(inquiry.getNote())
                .regDate(inquiry.getRegDate())
                .build();
    }
}
