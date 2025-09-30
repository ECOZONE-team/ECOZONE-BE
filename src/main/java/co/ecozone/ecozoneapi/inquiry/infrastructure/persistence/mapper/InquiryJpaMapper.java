package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.mapper;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity.InquiryJpaEntity;
import co.ecozone.ecozoneapi.common.config.MapStructCentralConfig;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(config = MapStructCentralConfig.class)
public interface InquiryJpaMapper {

    @ObjectFactory
    default Inquiry toDomain(InquiryJpaEntity e) {
        if (e == null) return null;
        return Inquiry.rehydrate(
                e.getId(),
                e.getCompanyIdx(),
                e.getCompanyName(),
                e.getName(),
                e.getPhone(),
                e.getNote(),
                e.getCreatedBy(),
                e.isAnswered(),
                e.getCreatedAt()
        );
    }

    @ObjectFactory
    default InquiryJpaEntity toEntity(Inquiry i) {
        if (i == null) return null;
        if (i.getId() == null) {
            return InquiryJpaEntity.of(
                    i.getCompanyIdx(),
                    i.getCompanyName(),
                    i.getName(),
                    i.getPhone(),
                    i.getNote(),
                    i.getCreatedBy(),
                    i.getCreatedAt()
            );
        }

        return InquiryJpaEntity.rehydrate(
                i.getId(),
                i.getCompanyIdx(),
                i.getCompanyName(),
                i.getName(),
                i.getPhone(),
                i.getNote(),
                i.getCreatedBy(),
                i.isAnswered(),
                i.getCreatedAt()
        );
    }
}
