package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inquiry")
public class InquiryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long id;

    @Column(nullable = false)
    private Long companyIdx;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(length = 2000)
    private String note;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private boolean answered = false;

    @Column(name = "reg_date", nullable = false)
    private Instant createdAt;

    protected InquiryJpaEntity() {}

    public static InquiryJpaEntity fromDomain(Inquiry inquiry) {
        InquiryJpaEntity e = new InquiryJpaEntity();
        e.id = inquiry.getId();
        e.companyIdx = inquiry.getCompanyIdx();
        e.companyName = inquiry.getCompanyName();
        e.name = inquiry.getName();
        e.phone = inquiry.getPhone();
        e.note = inquiry.getNote();
        e.createdBy = inquiry.getCreatedBy();
        e.answered = inquiry.isAnswered();
        e.createdAt = inquiry.getCreatedAt();
        return e;
    }

    public Inquiry toDomain() {
        return Inquiry.rehydrate(
                id, companyIdx, companyName, name, phone, note, createdBy, answered, createdAt
        );
    }
}
