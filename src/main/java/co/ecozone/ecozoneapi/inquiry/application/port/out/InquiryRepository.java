package co.ecozone.ecozoneapi.inquiry.application.port.out;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import java.util.List;
import java.util.Optional;

public interface InquiryRepository {
    Inquiry save(Inquiry inquiry);
    Optional<Inquiry> findById(Long id);
    List<Inquiry> findByCreatedBy(Long createdBy);
    List<Inquiry> findAll();
}
