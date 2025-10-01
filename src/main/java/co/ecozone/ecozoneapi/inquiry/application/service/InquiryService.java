package co.ecozone.ecozoneapi.inquiry.application.service;

import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.command.UpdateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.port.out.InquiryRepository;
import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryDetailResponse;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryListResponse;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper.InquiryApiMapper;
import co.ecozone.ecozoneapi.platform.web.error.ApiException;
import co.ecozone.ecozoneapi.platform.web.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository repository;
    private final InquiryApiMapper mapper;
    private final Clock clock;

    @Transactional
    public InquiryDetailResponse createInquiry(CreateInquiryCommand cmd) {
        try {
            Inquiry inquiry = Inquiry.create(
                    cmd.companyIdx(),
                    cmd.companyName(),
                    cmd.name(),
                    cmd.phone(),
                    cmd.note(),
                    cmd.createdBy(),
                    clock.instant()
            );
            Inquiry saved = repository.save(inquiry);
            return mapper.toDetailResponse(saved);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "문의 생성 중 오류가 발생했습니다.");
        }
    }

    public Page<InquiryListResponse> listInquiries(Long requesterId, boolean isAdmin, Pageable pageable) {
        List<InquiryListResponse> list = (isAdmin ? repository.findAll(pageable)
                : repository.findByCreatedBy(requesterId, pageable))
                .stream()
                .map(mapper::toSummaryResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, list.size());
    }

    public InquiryDetailResponse getInquiry(Long id, Long requesterId, boolean isAdmin) {
        Inquiry inquiry = repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.INQUIRY_NOT_FOUND));

        if (!isAdmin && !inquiry.getCreatedBy().equals(requesterId)) {
            throw new ApiException(ErrorCode.INQUIRY_NOT_FOUND, "접근 권한이 없습니다.");
        }

        return mapper.toDetailResponse(inquiry);
    }

    @Transactional
    public InquiryDetailResponse markAsAnswered(Long id, Long requesterId) {
        Inquiry inquiry = repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.INQUIRY_NOT_FOUND));
        Inquiry updated = inquiry.markAsAnswered();
        Inquiry saved = repository.save(updated);
        return mapper.toDetailResponse(saved);
    }

    @Transactional
    public InquiryDetailResponse updateInquiry(UpdateInquiryCommand cmd, Long requesterId) {
        Inquiry inquiry = repository.findById(cmd.id())
                .orElseThrow(() -> new ApiException(ErrorCode.INQUIRY_NOT_FOUND));

        if (!inquiry.getCreatedBy().equals(requesterId)) {
            throw new ApiException(ErrorCode.INQUIRY_NOT_FOUND, "문의 작성자만 수정 가능합니다.");
        }

        Inquiry updated = inquiry.update(cmd.name(), cmd.phone(), cmd.note());
        Inquiry saved = repository.save(updated);
        return mapper.toDetailResponse(saved);
    }
}
