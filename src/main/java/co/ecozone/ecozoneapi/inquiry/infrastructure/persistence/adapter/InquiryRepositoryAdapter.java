package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.adapter;

import co.ecozone.ecozoneapi.inquiry.application.port.out.InquiryRepository;
import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.mapper.InquiryJpaMapper;
import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.repository.SpringDataInquiryJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InquiryRepositoryAdapter implements InquiryRepository {

    private final SpringDataInquiryJpaRepository springData;
    private final InquiryJpaMapper mapper;

    public InquiryRepositoryAdapter(SpringDataInquiryJpaRepository springData, InquiryJpaMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Inquiry save(Inquiry inquiry) {
        return mapper.toDomain(
                springData.save(mapper.toEntity(inquiry))
        );
    }

    @Override
    public Optional<Inquiry> findById(Long id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Inquiry> findByCreatedBy(Long createdBy, Pageable pageable) {
        return springData.findByCreatedBy(createdBy, pageable)
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inquiry> findAll(Pageable pageable) {
        return springData.findAll(pageable)
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
