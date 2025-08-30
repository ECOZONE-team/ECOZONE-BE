package co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper;

import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquiryDetail;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquirySummary;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryCreateRequest;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryDetailResponse;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryListResponse;
import co.ecozone.ecozoneapi.common.config.MapStructCentralConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructCentralConfig.class)
public interface InquiryApiMapper {

    CreateInquiryCommand toCommand(InquiryCreateRequest req, Long createdBy);

    InquiryListResponse toResponse(InquirySummary summary);

    InquiryDetailResponse toResponse(InquiryDetail detail);
}
