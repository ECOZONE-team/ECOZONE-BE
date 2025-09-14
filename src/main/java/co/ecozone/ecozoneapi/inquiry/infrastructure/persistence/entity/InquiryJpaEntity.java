package co.ecozone.ecozoneapi.inquiry.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.Instant;

@Getter
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

    public static InquiryJpaEntity of(Long companyIdx, String companyName, String name,
                                      String phone, String note, Long createdBy, Instant createdAt) {
        InquiryJpaEntity e = new InquiryJpaEntity();
        e.companyIdx = companyIdx;
        e.companyName = companyName;
        e.name = name;
        e.phone = phone;
        e.note = note;
        e.createdBy = createdBy;
        e.answered = false;
        e.createdAt = createdAt;
        return e;
    }

    public static InquiryJpaEntity rehydrate(Long id, Long companyIdx, String companyName,
                                             String name, String phone, String note,
                                             Long createdBy, boolean answered, Instant createdAt) {
        InquiryJpaEntity e = new InquiryJpaEntity();
        e.id = id;
        e.companyIdx = companyIdx;
        e.companyName = companyName;
        e.name = name;
        e.phone = phone;
        e.note = note;
        e.createdBy = createdBy;
        e.answered = answered;
        e.createdAt = createdAt;
        return e;
    }
}
