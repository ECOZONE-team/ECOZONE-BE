package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.repository;

import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity.InquiryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataInquiryJpaRepository extends JpaRepository<InquiryJpaEntity, Long> {

    Page<InquiryJpaEntity> findByCreatedBy(Long createdBy, Pageable pageable);
    Page<InquiryJpaEntity> findAll(Pageable pageable);
}
