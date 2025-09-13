package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.adapter;

import co.ecozone.ecozoneapi.inquiry.application.port.out.InquiryRepository;
import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity.InquiryJpaEntity;
import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.repository.SpringDataInquiryJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InquiryRepositoryAdapter implements InquiryRepository {

    private final SpringDataInquiryJpaRepository springData;
    private final Clock clock;

    public InquiryRepositoryAdapter(SpringDataInquiryJpaRepository springData, Clock clock) {
        this.springData = springData;
        this.clock = clock;
    }

    @Override
    public Inquiry save(Inquiry inquiry) {
        InquiryJpaEntity entity = InquiryJpaEntity.fromDomain(inquiry);
        return springData.save(entity).toDomain();
    }

    @Override
    public Optional<Inquiry> findById(Long id) {
        return springData.findById(id).map(InquiryJpaEntity::toDomain);
    }

    @Override
    public List<Inquiry> findByCreatedBy(Long createdBy) {
        return springData.findByCreatedBy(createdBy)
                .stream().map(InquiryJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Inquiry> findAll() {
        return springData.findAll().stream().map(InquiryJpaEntity::toDomain).collect(Collectors.toList());
    }
}
