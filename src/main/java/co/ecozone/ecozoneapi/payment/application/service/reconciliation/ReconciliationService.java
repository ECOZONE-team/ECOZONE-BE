package co.ecozone.ecozoneapi.payment.application.service.reconciliation;

import co.ecozone.ecozoneapi.payment.application.port.out.PaymentGatewayQueryPort;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.application.port.out.ReconciliationRepository;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import co.ecozone.ecozoneapi.payment.domain.model.ReconciliationEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 조정(Reconciliation) 서비스
 * - 내부 시스템과 PG 간 거래 대사
 * - 불일치 건 자동 조정 또는 수동 조정 요청
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final PaymentRepository paymentRepository;
    private final ReconciliationRepository reconciliationRepository;
    private final PaymentGatewayQueryPort pgQueryPort;
    private final Clock clock;

    /**
     * 특정 기간의 거래 조정 실행
     *
     * @param from 시작 시각
     * @param to 종료 시각
     * @return 조정 배치 ID
     */
    @Transactional
    public String reconcile(Instant from, Instant to) {
        String batchId = generateBatchId();
        Instant now = Instant.now(clock);

        log.info("Starting reconciliation: batchId={}, from={}, to={}", batchId, from, to);

        try {
            // 1) PG에서 거래 내역 조회
            List<PaymentGatewayQueryPort.PGTransaction> pgTransactions = pgQueryPort.fetchTransactions(from, to);
            log.info("Fetched {} transactions from PG", pgTransactions.size());

            // 2) 내부 시스템의 거래 조회
            List<Payment> internalPayments = paymentRepository.findByDateRange(from, to);
            log.info("Fetched {} payments from internal system", internalPayments.size());

            // 3) orderId 기준으로 매핑
            Map<String, Payment> internalMap = new HashMap<>();
            for (Payment p : internalPayments) {
                internalMap.put(p.getOrderId(), p);
            }

            Map<String, PaymentGatewayQueryPort.PGTransaction> pgMap = new HashMap<>();
            for (var pg : pgTransactions) {
                pgMap.put(pg.orderId(), pg);
            }

            // 4) 대사 실행
            Set<String> allOrderIds = new HashSet<>();
            allOrderIds.addAll(internalMap.keySet());
            allOrderIds.addAll(pgMap.keySet());

            int matchedCount = 0;
            int discrepancyCount = 0;
            int missingInPgCount = 0;
            int missingInSystemCount = 0;

            for (String orderId : allOrderIds) {
                Payment internal = internalMap.get(orderId);
                PaymentGatewayQueryPort.PGTransaction pg = pgMap.get(orderId);

                ReconciliationEntry entry;

                if (internal == null) {
                    // PG에만 존재 (내부 시스템에 없음)
                    entry = ReconciliationEntry.discrepancy(
                            orderId,
                            batchId,
                            ReconciliationEntry.DiscrepancyType.MISSING,
                            null,
                            null,
                            null,
                            pg.paymentKey(),
                            pg.status(),
                            pg.amount(),
                            "Missing in internal system",
                            now
                    );
                    missingInSystemCount++;

                } else if (pg == null) {
                    // 내부 시스템에만 존재 (PG에 없음)
                    entry = ReconciliationEntry.discrepancy(
                            orderId,
                            batchId,
                            ReconciliationEntry.DiscrepancyType.MISSING,
                            internal.getId(),
                            internal.getStatus(),
                            internal.getAmount(),
                            null,
                            null,
                            null,
                            "Missing in PG",
                            now
                    );
                    missingInPgCount++;

                } else {
                    // 양쪽에 모두 존재 - 비교
                    entry = compareAndReconcile(orderId, batchId, internal, pg, now);
                    if (entry.isMatched()) {
                        matchedCount++;
                    } else {
                        discrepancyCount++;
                    }
                }

                reconciliationRepository.save(entry);
            }

            log.info("Reconciliation completed: batchId={}, matched={}, discrepancy={}, missingInPG={}, missingInSystem={}",
                    batchId, matchedCount, discrepancyCount, missingInPgCount, missingInSystemCount);

            return batchId;

        } catch (Exception e) {
            log.error("Reconciliation failed: batchId={}, error={}", batchId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 내부 시스템과 PG 거래 비교
     */
    private ReconciliationEntry compareAndReconcile(
            String orderId,
            String batchId,
            Payment internal,
            PaymentGatewayQueryPort.PGTransaction pg,
            Instant now
    ) {
        // 금액 비교
        if (internal.getAmount() != pg.amount()) {
            return ReconciliationEntry.discrepancy(
                    orderId,
                    batchId,
                    ReconciliationEntry.DiscrepancyType.AMOUNT_MISMATCH,
                    internal.getId(),
                    internal.getStatus(),
                    internal.getAmount(),
                    pg.paymentKey(),
                    pg.status(),
                    pg.amount(),
                    String.format("Amount mismatch: internal=%d, pg=%d", internal.getAmount(), pg.amount()),
                    now
            );
        }

        // 상태 비교
        String pgStatusMapped = mapPGStatus(pg.status());
        if (!internal.getStatus().name().equals(pgStatusMapped)) {
            // 상태 불일치 - 자동 조정 시도
            if (canAutoResolve(internal.getStatus(), pgStatusMapped)) {
                ReconciliationEntry entry = ReconciliationEntry.discrepancy(
                        orderId,
                        batchId,
                        ReconciliationEntry.DiscrepancyType.STATUS_MISMATCH,
                        internal.getId(),
                        internal.getStatus(),
                        internal.getAmount(),
                        pg.paymentKey(),
                        pg.status(),
                        pg.amount(),
                        String.format("Status mismatch: internal=%s, pg=%s", internal.getStatus(), pg.status()),
                        now
                );

                // 자동 조정 (예: PG가 CONFIRMED인데 내부가 AUTHORIZED면 동기화)
                return entry.autoResolve("Auto-resolved: synced status from PG", now);
            } else {
                // 수동 조정 필요
                ReconciliationEntry entry = ReconciliationEntry.discrepancy(
                        orderId,
                        batchId,
                        ReconciliationEntry.DiscrepancyType.STATUS_MISMATCH,
                        internal.getId(),
                        internal.getStatus(),
                        internal.getAmount(),
                        pg.paymentKey(),
                        pg.status(),
                        pg.amount(),
                        String.format("Status mismatch: internal=%s, pg=%s (manual required)", internal.getStatus(), pg.status()),
                        now
                );
                return entry.markManualRequired("Manual intervention required for status mismatch", now);
            }
        }

        // 일치
        return ReconciliationEntry.matched(
                orderId,
                batchId,
                internal.getId(),
                internal.getStatus(),
                internal.getAmount(),
                pg.paymentKey(),
                pg.status(),
                now
        );
    }

    /**
     * PG 상태를 내부 상태로 매핑
     */
    private String mapPGStatus(String pgStatus) {
        return switch (pgStatus.toUpperCase()) {
            case "DONE", "COMPLETED" -> "CONFIRMED";
            case "CANCELED", "CANCELLED" -> "CANCELED";
            case "FAILED" -> "FAILED";
            case "READY", "IN_PROGRESS" -> "AUTHORIZED";
            default -> pgStatus.toUpperCase();
        };
    }

    /**
     * 자동 조정 가능 여부 판단
     */
    private boolean canAutoResolve(PaymentStatus internalStatus, String pgStatus) {
        // PG가 CONFIRMED인데 내부가 AUTHORIZED면 자동 동기화 가능
        if (internalStatus == PaymentStatus.AUTHORIZED && "CONFIRMED".equals(pgStatus)) {
            return true;
        }
        // 그 외는 수동 조정 필요
        return false;
    }

    /**
     * 배치 ID 생성 (yyyyMMddHHmmss 형식)
     */
    private String generateBatchId() {
        Instant now = Instant.now(clock);
        return "RECON_" + now.truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "").replace("-", "");
    }

    /**
     * 수동 조정 필요 건 조회
     */
    @Transactional(readOnly = true)
    public List<ReconciliationEntry> getManualRequiredEntries() {
        return reconciliationRepository.findManualRequiredEntries();
    }

    /**
     * 불일치 건 조회
     */
    @Transactional(readOnly = true)
    public List<ReconciliationEntry> getDiscrepancies() {
        return reconciliationRepository.findDiscrepancies();
    }
}
