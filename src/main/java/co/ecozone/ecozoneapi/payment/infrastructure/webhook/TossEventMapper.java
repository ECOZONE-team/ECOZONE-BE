package co.ecozone.ecozoneapi.payment.infrastructure.webhook;

import co.ecozone.ecozoneapi.payment.domain.model.EventType;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import org.springframework.stereotype.Component;

/**
 * Toss Payments 이벤트 타입 매퍼
 * - 외부 API 타입 → 도메인 타입 변환
 * @since 2025-01-15
 */
@Component
public class TossEventMapper {

    /**
     * Toss 이벤트 타입을 도메인 이벤트 타입으로 변환
     *
     * @param tossEventType Toss API의 이벤트 타입 문자열
     * @return 도메인 이벤트 타입
     * @throws IllegalArgumentException 알 수 없는 이벤트 타입인 경우
     */
    public EventType toDomainEventType(String tossEventType) {
        if (tossEventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }

        return switch (tossEventType) {
            // 결제 관련
            case "PAYMENT_CONFIRMED", "payment.confirmed" -> EventType.PAYMENT_CONFIRMED;
            case "PAYMENT_CANCELED", "payment.canceled" -> EventType.PAYMENT_CANCELED;
            case "PAYMENT_FAILED", "payment.failed" -> EventType.PAYMENT_FAILED;

            // 가상계좌 관련
            case "VIRTUAL_ACCOUNT_ISSUED", "virtual_account.issued" -> EventType.VIRTUAL_ACCOUNT_ISSUED;
            case "VIRTUAL_ACCOUNT_DEPOSITED", "virtual_account.deposited" -> EventType.VIRTUAL_ACCOUNT_DEPOSITED;

            // 알 수 없는 타입
            default -> throw new IllegalArgumentException("Unknown Toss event type: " + tossEventType);
        };
    }
}
