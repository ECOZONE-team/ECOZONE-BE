package co.ecozone.ecozoneapi.payment.domain.model;


import java.time.Instant;


import java.time.Instant;
import java.util.Objects;

/** 결제 원장 엔트리(불변) */
public final class LedgerEntry {
    private final Long id;
    private final String orderId;
    private final String fromAccount; // 예: MERCHANT:eco
    private final String toAccount;   // 예: USER:{userId}
    private final long amount;
    private final Instant occurredAt;
    private final String memo;

    private LedgerEntry(Long id, String orderId, String fromAccount, String toAccount,
                        long amount, Instant occurredAt, String memo) {
        this.id = id;
        this.orderId = Objects.requireNonNull(orderId);
        this.fromAccount = Objects.requireNonNull(fromAccount);
        this.toAccount = Objects.requireNonNull(toAccount);
        this.amount = amount;
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.memo = memo;
    }

    public static LedgerEntry of(String orderId, String from, String to, long amount, Instant at, String memo) {
        return new LedgerEntry(null, orderId, from, to, amount, at, memo);
    }

    public static LedgerEntry rehydrate(Long id, String orderId, String from, String to, long amount, Instant at, String memo) {
        return new LedgerEntry(id, orderId, from, to, amount, at, memo);
    }

    public Long id() { return id; }
    public String orderId() { return orderId; }
    public String fromAccount() { return fromAccount; }
    public String toAccount() { return toAccount; }
    public long amount() { return amount; }
    public Instant occurredAt() { return occurredAt; }
    public String memo() { return memo; }
}
