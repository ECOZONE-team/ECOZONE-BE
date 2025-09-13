package co.ecozone.ecozoneapi.inquiry.application.service;

import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.command.UpdateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquiryDetail;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquirySummary;
import co.ecozone.ecozoneapi.inquiry.application.exception.InquiryAccessDeniedException;
import co.ecozone.ecozoneapi.inquiry.application.exception.InquiryNotFoundException;
import co.ecozone.ecozoneapi.inquiry.application.port.out.InquiryRepository;
import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
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
    private final Clock clock;

    @Transactional
    public InquiryDetail createInquiry(CreateInquiryCommand cmd) {
        Inquiry domain = Inquiry.create(cmd.companyIdx(), cmd.companyName(), cmd.name(),
                cmd.phone(), cmd.note(), cmd.createdBy(), clock.instant());
        Inquiry saved = repository.save(domain);
        return toDetail(saved);
    }

    public Page<InquirySummary> listInquiries(Long requesterId, boolean isAdmin, Pageable pageable) {
        List<Inquiry> list = isAdmin
                ? repository.findAll(pageable)
                : repository.findByCreatedBy(requesterId, pageable);

        List<InquirySummary> summaries = list.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, summaries.size());
    }

    public InquiryDetail getInquiry(Long id, Long requesterId, boolean isAdmin) {
        Inquiry inquiry = repository.findById(id)
                .orElseThrow(() -> new InquiryNotFoundException("Inquiry not found: " + id));

        if (!isAdmin && !inquiry.getCreatedBy().equals(requesterId)) {
            throw new InquiryAccessDeniedException("Access denied: " + id);
        }

        return toDetail(inquiry);
    }

    @Transactional
    public InquiryDetail markAsAnswered(Long id, Long requesterId) {
        Inquiry inquiry = repository.findById(id)
                .orElseThrow(() -> new InquiryNotFoundException("Inquiry not found: " + id));

        Inquiry saved = repository.save(inquiry.markAsAnswered());
        return toDetail(saved);
    }

    @Transactional
    public InquiryDetail updateInquiry(UpdateInquiryCommand cmd, Long requesterId) {
        Inquiry inquiry = repository.findById(cmd.id())
                .orElseThrow(() -> new InquiryNotFoundException("Inquiry not found: " + cmd.id()));

        if (!inquiry.getCreatedBy().equals(requesterId)) {
            throw new InquiryAccessDeniedException("Only the creator can update this inquiry: " + cmd.id());
        }

        Inquiry updated = repository.save(
                inquiry.update(cmd.name(), cmd.phone(), cmd.note())
        );
        return toDetail(updated);
    }

    private InquirySummary toSummary(Inquiry i) {
        return new InquirySummary(i.getId(), i.getCompanyName(), i.getName(), i.getCreatedAt(), i.isAnswered());
    }

    private InquiryDetail toDetail(Inquiry i) {
        return new InquiryDetail(i.getId(), i.getCompanyIdx(), i.getCompanyName(),
                i.getName(), i.getPhone(), i.getNote(), i.isAnswered(), i.getCreatedAt());
    }
}
