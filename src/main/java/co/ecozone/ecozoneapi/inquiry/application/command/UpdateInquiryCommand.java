package co.ecozone.ecozoneapi.inquiry.application.command;

public record UpdateInquiryCommand(
        Long id,
        String companyName,
        String name,
        String phone,
        String note,
        Long modifiedBy
) {}
