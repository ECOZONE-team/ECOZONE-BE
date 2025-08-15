package co.ecozone.ecozoneapi.inquiry.presentation.dto;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryListResponse {
    private Long idx;
    private String companyName;
    private String name;
    private LocalDateTime regDate;

    public static InquiryListResponse from(Inquiry inquiry) {
        return InquiryListResponse.builder()
                .idx(inquiry.getIdx())
                .companyName(inquiry.getCompanyName())
                .name(inquiry.getName())
                .regDate(inquiry.getRegDate())
                .build();
    }
}
