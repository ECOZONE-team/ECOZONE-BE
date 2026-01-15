package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.payment.application.port.out.PaymentEventRepository;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.LedgerEntry;
import co.ecozone.ecozoneapi.payment.domain.model.Payment;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentEvent;
import co.ecozone.ecozoneapi.payment.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;

/**
 * Webhook 이벤트 처리 서비스
 * - PG로부터 수신한 이벤트를 처리
 * - 멱등성 보장 (eventId 기반)
 * - REQUIRES_NEW 트랜잭션으로 독립 처리
 * @since 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentEventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final PlatformTransactionManager txm;
    private final Clock clock;

    private TransactionTemplate txNew() {
        TransactionTemplate t = new TransactionTemplate(txm);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return t;
    }

    /**
     * Webhook 이벤트 처리 (멱등성 보장)
     *
     * @param eventId PG 이벤트 ID (멱등키)
     * @param eventType 이벤트 타입
     * @param orderId 주문번호
     * @param paymentKey PG 결제키
     * @param payload 원본 JSON 페이로드
     * @param occurredAt PG 이벤트 발생 시각
     * @return 처리 결과 이벤트 ID
     */
    @Transactional
    public Long handleWebhook(
            String eventId,
            PaymentEvent.EventType eventType,
            String orderId,
            String paymentKey,
            String payload,
            Instant occurredAt
    ) {
        final Instant now = Instant.now(clock);

        // 1) 이벤트 선점 (멱등성 체크) - REQUIRES_NEW
        PaymentEvent event = txNew().execute(status -> {
            return eventRepository.findByEventId(eventId)
                    .orElseGet(() -> {
                        // 새로운 이벤트 생성
                        PaymentEvent newEvent = PaymentEvent.create(
                                eventId, eventType, orderId, paymentKey, payload, occurredAt, now
                        );
                        return eventRepository.save(newEvent);
                    });
        });

        // 이미 처리된 이벤트면 바로 반환 (멱등)
        if (event.isProcessed()) {
            log.info("Webhook event already processed: eventId={}", eventId);
            return event.getId();
        }

        // 2) 이벤트 타입별 처리
        try {
            switch (eventType) {
                case PAYMENT_CONFIRMED -> handlePaymentConfirmed(orderId, paymentKey, now);
                case PAYMENT_CANCELED -> handlePaymentCanceled(orderId, paymentKey, now);
                case PAYMENT_FAILED -> handlePaymentFailed(orderId, now);
                default -> log.warn("Unsupported webhook event type: {}", eventType);
            }

            // 3) 이벤트 처리 완료 - REQUIRES_NEW
            txNew().executeWithoutResult(st -> {
                PaymentEvent managed = eventRepository.findByEventId(eventId).orElseThrow();
                eventRepository.save(managed.markProcessed(Instant.now(clock)));
            });

            return event.getId();

        } catch (Exception e) {
            log.error("Failed to process webhook event: eventId={}, error={}", eventId, e.getMessage(), e);

            // 이벤트 처리 실패 기록
            txNew().executeWithoutResult(st -> {
                PaymentEvent managed = eventRepository.findByEventId(eventId).orElseThrow();
                eventRepository.save(managed.markFailed(e.getMessage(), Instant.now(clock)));
            });

            throw e;
        }
    }

    /**
     * PAYMENT_CONFIRMED 이벤트 처리
     */
    private void handlePaymentConfirmed(String orderId, String paymentKey, Instant now) {
        txNew().executeWithoutResult(st -> {
            Payment payment = paymentRepository.lockByOrderId(orderId)
                    .orElseThrow(() -> new IllegalStateException("Payment not found: " + orderId));

            // 이미 terminal 상태면 처리하지 않음
            if (payment.isTerminal()) {
                log.info("Payment already in terminal state: orderId={}, status={}", orderId, payment.getStatus());
                return;
            }

            // CONFIRMED 상태로 전이
            Payment confirmed = payment.confirmed(now, now);
            paymentRepository.save(confirmed);

            // 원장 기록
            paymentRepository.append(LedgerEntry.of(
                    orderId,
                    "USER:" + payment.getUserId(),
                    "MERCHANT:eco",
                    payment.getAmount(),
                    now,
                    "webhook:confirmed"
            ));

            log.info("Payment confirmed via webhook: orderId={}, paymentKey={}", orderId, paymentKey);
        });
    }

    /**
     * PAYMENT_CANCELED 이벤트 처리
     */
    private void handlePaymentCanceled(String orderId, String paymentKey, Instant now) {
        txNew().executeWithoutResult(st -> {
            Payment payment = paymentRepository.lockByOrderId(orderId)
                    .orElseThrow(() -> new IllegalStateException("Payment not found: " + orderId));

            // CANCELED 상태가 아니면 전이
            if (payment.getStatus() != PaymentStatus.CANCELED) {
                Payment canceled = payment.canceled("webhook:canceled", now);
                paymentRepository.save(canceled);

                // 원장 기록 (환불)
                paymentRepository.append(LedgerEntry.of(
                        orderId,
                        "MERCHANT:eco",
                        "USER:" + payment.getUserId(),
                        payment.getAmount(),
                        now,
                        "webhook:canceled"
                ));

                log.info("Payment canceled via webhook: orderId={}, paymentKey={}", orderId, paymentKey);
            }
        });
    }

    /**
     * PAYMENT_FAILED 이벤트 처리
     */
    private void handlePaymentFailed(String orderId, Instant now) {
        txNew().executeWithoutResult(st -> {
            Payment payment = paymentRepository.lockByOrderId(orderId)
                    .orElseThrow(() -> new IllegalStateException("Payment not found: " + orderId));

            if (!payment.isTerminal()) {
                Payment failed = payment.failed("webhook:failed", now);
                paymentRepository.save(failed);
                log.info("Payment failed via webhook: orderId={}", orderId);
            }
        });
    }
}
