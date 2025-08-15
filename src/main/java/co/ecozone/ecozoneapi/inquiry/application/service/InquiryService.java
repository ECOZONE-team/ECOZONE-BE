package co.ecozone.ecozoneapi.inquiry.application.service;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.infrastructure.repository.InquiryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InquiryService {

    private final InquiryRepository repository;

    public InquiryService(InquiryRepository repository) {
        this.repository = repository;
    }

    public Inquiry createInquiry(Inquiry inquiry) {
        return repository.save(inquiry);
    }

    public List<Inquiry> getInquiries(String username, boolean isAdmin) {
        if (isAdmin) {
            return repository.findAll();
        } else {
            return repository.findByCreatedBy(username);
        }
    }

    public Inquiry getInquiry(Long id, String username, boolean isAdmin) {
        Inquiry inquiry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        if (!isAdmin && !inquiry.getCreatedBy().equals(username)) {
            throw new RuntimeException("권한 없음");
        }
        return inquiry;
    }
}
