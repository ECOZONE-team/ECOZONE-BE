package co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper;

import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.*;
import co.ecozone.ecozoneapi.common.config.MapStructCentralConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructCentralConfig.class)
public interface InquiryApiMapper {

    CreateInquiryCommand toCommand(InquiryCreateRequest req, Long createdBy);

    InquiryDetailResponse toDetailResponse(Inquiry inquiry);

    InquiryListResponse toSummaryResponse(Inquiry inquiry);
}
