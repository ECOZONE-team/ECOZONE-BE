package co.ecozone.ecozoneapi.inquiry.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public final class Inquiry {
    private final Long id;
    private final Long companyIdx;
    private final String companyName;
    private final String name;
    private final String phone;
    private final String note;
    private final Long createdBy;
    private final boolean answered;
    private final Instant createdAt;

    private Inquiry(Long id, Long companyIdx, String companyName, String name,
                    String phone, String note, Long createdBy, boolean answered, Instant createdAt) {
        this.id = id;
        this.companyIdx = Objects.requireNonNull(companyIdx);
        this.companyName = Objects.requireNonNull(companyName);
        this.name = Objects.requireNonNull(name);
        this.phone = Objects.requireNonNull(phone);
        this.note = note;
        this.createdBy = Objects.requireNonNull(createdBy);
        this.answered = answered;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Inquiry create(Long companyIdx, String companyName, String name,
                                 String phone, String note, Long createdBy, Instant now) {
        return new Inquiry(null, companyIdx, companyName, name, phone, note, createdBy, false, now);
    }

    public static Inquiry rehydrate(Long id, Long companyIdx, String companyName, String name,
                                    String phone, String note, Long createdBy, boolean answered, Instant createdAt) {
        return new Inquiry(id, companyIdx, companyName, name, phone, note, createdBy, answered, createdAt);
    }

    public Inquiry markAsAnswered() {
        if (answered) return this;
        return new Inquiry(id, companyIdx, companyName, name, phone, note, createdBy, true, createdAt);
    }
}
