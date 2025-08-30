package co.ecozone.ecozoneapi.inquiry.application.command;

public record CreateInquiryCommand(
        Long companyIdx,
        String companyName,
        String name,
        String phone,
        String note,
        Long createdBy
) {}
