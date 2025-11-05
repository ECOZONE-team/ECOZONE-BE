package co.ecozone.ecozoneapi.payment.domain.model;

import java.time.Instant;
import java.util.Objects;

/** 지갑(머천트/사용자 잔액 스냅샷용) – 필요 시 사용 */
public final class WalletEntry {
    private final Long id;
    private final String accountId;     // "USER:{userId}" / "MERCHANT:{id}"
    private final long delta;           // +증가 / -감소
    private final long balanceAfter;    // 이 엔트리 반영 후 잔액(옵션)
    private final Instant occurredAt;
    private final String orderId;
    private final String memo;

    private WalletEntry(Long id, String accountId, long delta, long balanceAfter,
                        Instant occurredAt, String orderId, String memo) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId);
        this.delta = delta;
        this.balanceAfter = balanceAfter;
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.orderId = Objects.requireNonNull(orderId);
        this.memo = memo;
    }

    public static WalletEntry of(String accountId, long delta, long balanceAfter,
                                 Instant at, String orderId, String memo) {
        return new WalletEntry(null, accountId, delta, balanceAfter, at, orderId, memo);
    }

    public static WalletEntry rehydrate(Long id, String accountId, long delta, long balanceAfter,
                                        Instant at, String orderId, String memo) {
        return new WalletEntry(id, accountId, delta, balanceAfter, at, orderId, memo);
    }

    public Long id() { return id; }
    public String accountId() { return accountId; }
    public long delta() { return delta; }
    public long balanceAfter() { return balanceAfter; }
    public Instant occurredAt() { return occurredAt; }
    public String orderId() { return orderId; }
    public String memo() { return memo; }
}

