package co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.payment.domain.model.LedgerEntry;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payment_ledger")
public class LedgerEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String orderId;

    @Column(nullable = false, length = 64)
    private String fromAccount;

    @Column(nullable = false, length = 64)
    private String toAccount;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(length = 255)
    private String memo;

    protected LedgerEntryJpaEntity() {}

    public static LedgerEntryJpaEntity from(LedgerEntry d) {
        LedgerEntryJpaEntity e = new LedgerEntryJpaEntity();
        e.id = d.id();
        e.orderId = d.orderId();
        e.fromAccount = d.fromAccount();
        e.toAccount = d.toAccount();
        e.amount = d.amount();
        e.occurredAt = d.occurredAt();
        e.memo = d.memo();
        return e;
    }

    public LedgerEntry toDomain() {
        return LedgerEntry.rehydrate(id, orderId, fromAccount, toAccount, amount, occurredAt, memo);
    }
}

