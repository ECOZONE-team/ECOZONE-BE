package co.ecozone.ecozoneapi.payment.application.service.reconciliation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 조정(Reconciliation) 배치 서비스
 * - 주기적으로 PG와 내부 시스템 간 거래 대사 실행
 * - Cron 표현식: 매일 새벽 2시 실행 (KST 기준)
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.reconciliation.enabled", havingValue = "true", matchIfMissing = true)
public class ReconciliationBatchService {

    private final ReconciliationService reconciliationService;
    private final Clock clock;

    /**
     * 매일 새벽 2시 (KST) - 전일 거래 대사
     * Cron: 초 분 시 일 월 요일
     * 0 0 2 * * * = 매일 2시 0분 0초
     */
    @Scheduled(cron = "${payment.reconciliation.cron:0 0 2 * * *}")
    public void runDailyReconciliation() {
        log.info("Starting daily reconciliation batch...");

        try {
            Instant now = Instant.now(clock);

            // 전일 00:00 ~ 23:59:59
            Instant yesterdayStart = now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            Instant yesterdayEnd = yesterdayStart.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.SECONDS);

            String batchId = reconciliationService.reconcile(yesterdayStart, yesterdayEnd);

            log.info("Daily reconciliation completed successfully: batchId={}", batchId);

        } catch (Exception e) {
            log.error("Daily reconciliation failed: {}", e.getMessage(), e);
            // TODO: 알림 전송 (Slack, Email 등)
        }
    }

    /**
     * 수동 실행용 - 특정 기간 대사
     */
    public String runManualReconciliation(Instant from, Instant to) {
        log.info("Starting manual reconciliation: from={}, to={}", from, to);
        return reconciliationService.reconcile(from, to);
    }

    /**
     * 지난 N일 재대사
     */
    public void reReconcileLastNDays(int days) {
        log.info("Re-reconciling last {} days...", days);

        Instant now = Instant.now(clock);
        Instant from = now.minus(days, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant to = now;

        reconciliationService.reconcile(from, to);
        log.info("Re-reconciliation completed for last {} days", days);
    }
}
