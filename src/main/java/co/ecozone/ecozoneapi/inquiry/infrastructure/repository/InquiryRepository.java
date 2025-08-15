package co.ecozone.ecozoneapi.inquiry.infrastructure.repository;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByCreatedBy(String createdBy);
}
