package co.ecozone.ecozoneapi.payment.application.service;

import co.ecozone.ecozoneapi.payment.application.port.out.PaymentEventRepository;
import co.ecozone.ecozoneapi.payment.application.port.out.PaymentRepository;
import co.ecozone.ecozoneapi.payment.domain.model.*;
import co.ecozone.ecozoneapi.payment.infrastructure.PaymentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;

import static co.ecozone.ecozoneapi.payment.domain.model.EventType.*;

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
    private final PaymentProperties paymentProperties;

    private TransactionTemplate txNew() {
        TransactionTemplate t = new TransactionTemplate(txm);
        t.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        t.setTimeout(30);
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
            EventType eventType,
            String orderId,
            String paymentKey,
            String payload,
            Instant occurredAt
    ) {
        final Instant now = Instant.now(clock);

        // 1) 이벤트 선점 (멱등성 체크) - REQUIRES_NEW
        PaymentEvent event = txNew().execute(status -> {
            // 동시성 제어: eventId UNIQUE 제약으로 중복 방지
            try {
                return eventRepository.findByEventId(eventId)
                        .orElseGet(() -> {
                            // 새로운 이벤트 생성
                            PaymentEvent newEvent = PaymentEvent.create(
                                    eventId, eventType, orderId, paymentKey, payload, occurredAt, now
                            );
                            return eventRepository.save(newEvent);
                        });
            } catch (DataIntegrityViolationException e) {
                // 동시에 같은 eventId가 저장되려 할 때 - 재조회
                log.debug("Duplicate eventId detected (concurrent insert): eventId={}", eventId);
                return eventRepository.findByEventId(eventId)
                        .orElseThrow(() -> new IllegalStateException("Event not found after duplicate: " + eventId));
            }
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
                default -> log.warn("Unsupported webhook event type: {}");
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
                String errorMsg = e.getMessage();
                // 에러 메시지 길이 제한
                if (errorMsg != null && errorMsg.length() > 300) {
                    errorMsg = errorMsg.substring(0, 300);
                }
                eventRepository.save(managed.markFailed(errorMsg, Instant.now(clock)));
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

            // paymentKey 검증
            if (payment.getPaymentKey() != null && paymentKey != null
                    && !payment.getPaymentKey().equals(paymentKey)) {
                throw new IllegalStateException("paymentKey mismatch: orderId=" + orderId);
            }

            // 이미 terminal 상태면 처리하지 않음
            if (payment.isTerminal()) {
                log.info("Payment already in terminal state: orderId={}, status={}", orderId, payment.getStatus());
                return;
            }

            // CONFIRMED 상태로 전이
            Payment confirmed = payment.confirmed(now, now);
            paymentRepository.save(confirmed);

            // 원장 기록
            PaymentProperties.Ledger ledgerConfig = paymentProperties.getLedger();
            paymentRepository.append(LedgerEntry.of(
                    orderId,
                    ledgerConfig.getUserAccountPrefix() + payment.getUserId(),
                    ledgerConfig.getMerchantAccountPrefix() + ledgerConfig.getMerchantId(),
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

            // paymentKey 검증
            if (payment.getPaymentKey() != null && paymentKey != null
                    && !payment.getPaymentKey().equals(paymentKey)) {
                throw new IllegalStateException("paymentKey mismatch: orderId=" + orderId);
            }

            // CANCELED 상태가 아니면 전이
            if (payment.getStatus() != PaymentStatus.CANCELED) {
                Payment canceled = payment.canceled("webhook:canceled", now);
                paymentRepository.save(canceled);

                // 원장 기록 (환불) - 하드코딩 제거
                PaymentProperties.Ledger ledgerConfig = paymentProperties.getLedger();
                paymentRepository.append(LedgerEntry.of(
                        orderId,
                        ledgerConfig.getMerchantAccountPrefix() + ledgerConfig.getMerchantId(),
                        ledgerConfig.getUserAccountPrefix() + payment.getUserId(),
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
