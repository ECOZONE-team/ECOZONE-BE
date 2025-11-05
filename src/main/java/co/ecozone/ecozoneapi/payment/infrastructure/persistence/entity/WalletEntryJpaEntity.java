package co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity;

import co.ecozone.ecozoneapi.payment.domain.model.WalletEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "wallet_entry", indexes = {
        @Index(name = "idx_wallet_account", columnList = "accountId"),
        @Index(name = "idx_wallet_order", columnList = "orderId")
})
@Getter
@NoArgsConstructor
public class WalletEntryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String accountId;

    @Column(nullable=false)
    private long delta;

    @Column(nullable=false)
    private long balanceAfter;

    @Column(nullable=false)
    private Instant occurredAt;

    @Column(nullable=false)
    private String orderId;

    @Column(length = 500)
    private String memo;

    public static WalletEntryJpaEntity from(WalletEntry e) {
        WalletEntryJpaEntity j = new WalletEntryJpaEntity();
        j.id = e.id();
        j.accountId = e.accountId();
        j.delta = e.delta();
        j.balanceAfter = e.balanceAfter();
        j.occurredAt = e.occurredAt();
        j.orderId = e.orderId();
        j.memo = e.memo();
        return j;
    }

    public WalletEntry toDomain() {
        return WalletEntry.rehydrate(id, accountId, delta, balanceAfter, occurredAt, orderId, memo);
    }
}

