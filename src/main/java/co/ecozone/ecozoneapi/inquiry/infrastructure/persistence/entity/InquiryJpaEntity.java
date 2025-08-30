package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Clock;
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

    /** Clock을 사용해서 createdAt 자동 생성 */
    public static InquiryJpaEntity of(Long companyIdx, String companyName, String name, String phone,
                                      String note, Long createdBy, Clock clock, boolean answered) {
        InquiryJpaEntity e = new InquiryJpaEntity();
        e.companyIdx = companyIdx;
        e.companyName = companyName;
        e.name = name;
        e.phone = phone;
        e.note = note;
        e.createdBy = createdBy;
        e.answered = answered;
        e.createdAt = Instant.now(clock); // Clock 사용
        return e;
    }

    public co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry toDomain() {
        return co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry.rehydrate(
                id, companyIdx, companyName, name, phone, note, createdBy, answered, createdAt
        );
    }
}
