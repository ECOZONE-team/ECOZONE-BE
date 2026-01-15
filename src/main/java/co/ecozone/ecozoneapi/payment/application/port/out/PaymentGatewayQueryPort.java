package co.ecozone.ecozoneapi.payment.application.port.out;

import java.time.Instant;
import java.util.List;

/**
 * PG 거래 내역 조회 포트
 * - Reconciliation용 PG 거래 조회
 * @since 2025-01-15
 */
public interface PaymentGatewayQueryPort {

    /**
     * PG 거래 정보
     */
    record PGTransaction(
            String orderId,
            String paymentKey,
            String status,
            long amount,
            Instant approvedAt,
            Instant canceledAt
    ) {}

    /**
     * 특정 기간의 PG 거래 내역 조회
     */
    List<PGTransaction> fetchTransactions(Instant from, Instant to);

    /**
     * 특정 orderId의 PG 거래 조회
     */
    PGTransaction fetchByOrderId(String orderId);
}
