package co.ecozone.ecozoneapi.payment.infrastructure.persistence;


import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.LedgerEntry;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.WalletEntry;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.LedgerEntryJpaEntity;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.entity.WalletEntryJpaEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 결제 리포지토리 어댑터(JPA)
 * - 저장 전략: 신규 persist, 기존 find→applyFrom→flush (분리/버전 오류 방지)
 * - lockByOrderId 등 직렬화 보장을 위한 잠금 쿼리 제공
 * @since 2025-09-16
 */
@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository paymentRepo;
    private final SpringDataLedgerRepository ledgerRepo;
    private final SpringDataWalletRepository walletRepo;
    private final Clock clock;

    @Override
    public Payment save(Payment p) {
        if (p.getId() == null) {
            // INSERT 경로: 새 엔티티를 persist
            PaymentJpaEntity saved = paymentRepo.save(PaymentJpaEntity.newOf(p));
            return saved.toDomain();
        } else {
            // UPDATE 경로: 기존 Managed 엔티티를 불러서 apply
            var managed = paymentRepo.findById(p.getId())
                    .orElseThrow(() -> new IllegalArgumentException("payment not found: " + p.getId()));
            managed.applyFrom(p, Instant.now(clock));
            // 별도 save() 불필요(Dirty Checking)지만, 명시적 save()도 무해
            return paymentRepo.save(managed).toDomain();
        }
    }

    /** 확인 단계에서 경합 방지용 */
    public Optional<Payment> lockByOrderId(String orderId) {
        return paymentRepo.lockByOrderId(orderId).map(PaymentJpaEntity::toDomain);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentRepo.findById(id).map(PaymentJpaEntity::toDomain);
    }

    /** 이중부기: Ledger 기록 + 각 계정별 WalletEntry 기록 */
    @Override
    @Transactional
    public void append(LedgerEntry entry) {
        // 1) Ledger 저장 (항상 남긴다: 감사를 위한 단일 출처)
        LedgerEntryJpaEntity savedLedger = ledgerRepo.save(LedgerEntryJpaEntity.from(entry));

        // 2) Wallet 저장 (from: 음수, to: 양수)
        String from = entry.fromAccount();
        String to   = entry.toAccount();
        long amt    = entry.amount();
        Instant at  = entry.occurredAt();
        String memo = entry.memo();
        String orderId = entry.orderId();

        List<WalletEntryJpaEntity> latestWallets = walletRepo.findLatestByAccountIds(Arrays.asList(from, to));
        Map<String, Long> balanceMap = latestWallets.stream()
                .collect(Collectors.toMap(
                        WalletEntryJpaEntity::getAccountId,
                        WalletEntryJpaEntity::getBalanceAfter
                ));

        long fromPrev = balanceMap.getOrDefault(from, 0L);
        long toPrev = balanceMap.getOrDefault(to, 0L);

        WalletEntry fromEntry = WalletEntry.of(
                from, -amt, fromPrev - amt, at, orderId, memo
        );
        WalletEntry toEntry = WalletEntry.of(
                to, +amt, toPrev + amt, at, orderId, memo
        );

        // 배치 저장으로 성능 개선
        walletRepo.saveAll(Arrays.asList(
                WalletEntryJpaEntity.from(fromEntry),
                WalletEntryJpaEntity.from(toEntry)
        ));
    }

    @Override
    public List<LedgerEntry> findLedgerByOrderId(String orderId) {
        return ledgerRepo.findByOrderId(orderId)
                .stream().map(LedgerEntryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Payment> findByDateRange(Instant from, Instant to) {
        return paymentRepo.findByCreatedAtBetween(from, to)
                .stream()
                .map(PaymentJpaEntity::toDomain)
                .toList();
    }
}