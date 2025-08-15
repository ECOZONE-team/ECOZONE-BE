package co.ecozone.ecozoneapi.inquiry.presentation.dto;

import lombok.Getter;

@Getter
public class InquiryCreateRequest {
    public Long companyIdx;
    public String companyName;
    public String name;
    public String phone;
    public String note;
}