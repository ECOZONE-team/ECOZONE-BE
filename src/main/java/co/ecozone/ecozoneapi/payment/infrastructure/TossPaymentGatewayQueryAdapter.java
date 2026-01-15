package co.ecozone.ecozoneapi.payment.infrastructure;

import co.ecozone.ecozoneapi.payment.application.port.out.PaymentGatewayQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Toss PG 거래 내역 조회 어댑터
 * - Reconciliation용 PG 거래 조회
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentGatewayQueryAdapter implements PaymentGatewayQueryPort {

    @Qualifier("tossRestClient")
    private final RestClient tossRestClient;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    /**
     * 특정 기간의 PG 거래 내역 조회
     * Toss API: GET /v1/transactions?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd
     */
    @Override
    public List<PGTransaction> fetchTransactions(Instant from, Instant to) {
        String startDate = DATE_FORMATTER.format(from);
        String endDate = DATE_FORMATTER.format(to);

        log.info("Fetching PG transactions: from={}, to={}", startDate, endDate);

        try {
            // Toss Payments Transactions API 호출
            // 참고: 실제 API 스펙에 따라 조정 필요
            Map<String, Object> response = tossRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/transactions")
                            .queryParam("startDate", startDate)
                            .queryParam("endDate", endDate)
                            .queryParam("limit", 1000)  // 페이지네이션 고려 필요
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("transactions")) {
                log.warn("No transactions returned from PG");
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transactions = (List<Map<String, Object>>) response.get("transactions");

            return transactions.stream()
                    .map(this::mapToTransaction)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to fetch PG transactions: {}", e.getMessage(), e);
            // Reconciliation에서 예외 처리하도록 다시 던짐
            throw new RuntimeException("Failed to fetch PG transactions", e);
        }
    }

    /**
     * 특정 orderId의 PG 거래 조회
     * Toss API: GET /v1/payments/orders/{orderId}
     */
    @Override
    public PGTransaction fetchByOrderId(String orderId) {
        log.info("Fetching PG transaction by orderId: {}", orderId);

        try {
            Map<String, Object> response = tossRestClient.get()
                    .uri("/v1/payments/orders/{orderId}", orderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new IllegalArgumentException("Transaction not found in PG: " + orderId);
            }

            return mapToTransaction(response);

        } catch (Exception e) {
            log.error("Failed to fetch PG transaction for orderId={}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch PG transaction", e);
        }
    }

    /**
     * Toss API 응답을 PGTransaction으로 매핑
     */
    private PGTransaction mapToTransaction(Map<String, Object> data) {
        String orderId = (String) data.get("orderId");
        String paymentKey = (String) data.get("paymentKey");
        String status = (String) data.get("status");

        // amount는 Integer 또는 Long일 수 있음
        long amount = data.get("totalAmount") != null
                ? ((Number) data.get("totalAmount")).longValue()
                : 0L;

        Instant approvedAt = data.get("approvedAt") != null
                ? Instant.parse((String) data.get("approvedAt"))
                : null;

        Instant canceledAt = data.get("canceledAt") != null
                ? Instant.parse((String) data.get("canceledAt"))
                : null;

        return new PGTransaction(
                orderId,
                paymentKey,
                status,
                amount,
                approvedAt,
                canceledAt
        );
    }
}
