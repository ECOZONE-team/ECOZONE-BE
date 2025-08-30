package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.repository;

import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity.InquiryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataInquiryJpaRepository extends JpaRepository<InquiryJpaEntity, Long> {
    List<InquiryJpaEntity> findByCreatedBy(Long createdBy);
}
