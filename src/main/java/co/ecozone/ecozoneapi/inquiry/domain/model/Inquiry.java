package co.ecozone.ecozoneapi.inquiry.domain.model;

import lombok.Getter;
import java.time.Instant;

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

        if (companyIdx == null) throw new IllegalArgumentException("companyIdx is required.");
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("companyName is required.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required.");
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("phone is required.");
        if (createdBy == null) throw new IllegalArgumentException("createdBy is required.");
        if (createdAt == null) throw new IllegalArgumentException("createdAt is required.");

        this.id = id;
        this.companyIdx = companyIdx;
        this.companyName = companyName;
        this.name = name;
        this.phone = phone;
        this.note = note;
        this.createdBy = createdBy;
        this.answered = answered;
        this.createdAt = createdAt;
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

    public Inquiry update(String name, String phone, String note) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required.");
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("phone is required.");
        return new Inquiry(
                this.id,
                this.companyIdx,
                this.companyName,
                name,
                phone,
                note != null ? note : this.note,
                this.createdBy,
                this.answered,
                this.createdAt
        );
    }
}
