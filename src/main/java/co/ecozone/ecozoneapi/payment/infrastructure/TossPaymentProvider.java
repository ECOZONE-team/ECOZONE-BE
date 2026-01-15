package co.ecozone.ecozoneapi.payment.infrastructure;

import co.ecozone.ecozoneapi.payment.application.dto.CancelResult;
import co.ecozone.ecozoneapi.payment.application.dto.ConfirmResult;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentProvider;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.response.TossCancelResponse;
import co.ecozone.ecozoneapi.payment.infrastructure.persistence.response.TossConfirmResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Toss PG 연동 어댑터 (인프라)
 * - RestClient로 /v1/payments/confirm, /v1/payments/{key}/cancel 호출
 * - BasicAuth(username=secret_key, password="") 기반 인증
 * - 외부 응답을 ConfirmResult/CancelResult로 변환
 * @since 2025-09-16
 */
@Component
@AllArgsConstructor
public class TossPaymentProvider implements PaymentProvider {

    @Qualifier("tossRestClient")
    RestClient tossRestClient;

    @Override
    public ConfirmResult confirm(String paymentKey, String orderId, long amount) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                var res = tossRestClient.post()
                        .uri("/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                        .retrieve()
                        .body(TossConfirmResponse.class);
                return new ConfirmResult(true, res.approvedAt(), null);
            } catch (RestClientResponseException e) {
                // 4xx, 5xx HTTP 오류 - 재시도하지 않음
                return new ConfirmResult(false, null, e.getResponseBodyAsString());
            } catch (Exception e) {
                // 네트워크 오류 등 - 재시도
                lastException = e;
                retryCount++;
                if (retryCount < maxRetries) {
                    long sleepMs = (long) Math.pow(2, retryCount) * 200L; // 400ms, 800ms, 1600ms
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new ConfirmResult(false, null, "interrupted:" + ie.getMessage());
                    }
                }
            }
        }

        // 모든 재시도 실패
        return new ConfirmResult(false, null, "network_retry_exhausted:" + lastException.getMessage());
    }

    @Override
    public CancelResult cancel(String paymentKey, String reason) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            try {
                var res = tossRestClient.post()
                        .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("cancelReason", Optional.ofNullable(reason).orElse("user request")))
                        .retrieve()
                        .body(TossCancelResponse.class);
                return new CancelResult(true, res.canceledAt(), null);
            } catch (RestClientResponseException e) {
                // 4xx, 5xx HTTP 오류 - 재시도하지 않음
                return new CancelResult(false, null, e.getResponseBodyAsString());
            } catch (Exception e) {
                // 네트워크 오류 등 - 재시도
                lastException = e;
                retryCount++;
                if (retryCount < maxRetries) {
                    long sleepMs = (long) Math.pow(2, retryCount) * 200L;
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new CancelResult(false, null, "interrupted:" + ie.getMessage());
                    }
                }
            }
        }

        // 모든 재시도 실패
        return new CancelResult(false, null, "network_retry_exhausted:" + lastException.getMessage());
    }

}