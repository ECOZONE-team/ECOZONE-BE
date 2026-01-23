package co.ecozone.ecozoneapi.payment.infrastructure.webhook;

import co.ecozone.ecozoneapi.payment.infrastructure.PaymentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Toss Payments Webhook 서명 검증기
 * - HMAC-SHA256 기반 서명 검증
 * - Timing Attack 방어 (Constant-time 비교)
 * @since 2025-01-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossWebhookVerifier {

    private final PaymentProperties paymentProperties;

    /**
     * Toss 웹훅 서명 검증
     *
     * @param signature X-Toss-Signature 헤더 값
     * @param body 요청 본문 (raw string)
     * @return 서명이 유효하면 true
     */
    public boolean verify(String signature, String body) {
        if (signature == null || body == null) {
            log.warn("Signature or body is null");
            return false;
        }

        try {
            String computed = computeSignature(body);

            // Constant-time 비교로 타이밍 공격 방어
            boolean valid = MessageDigest.isEqual(
                    signature.getBytes(StandardCharsets.UTF_8),
                    computed.getBytes(StandardCharsets.UTF_8)
            );

            if (!valid) {
                log.warn("Signature verification failed: expected={}, actual={}",
                        maskSignature(computed), maskSignature(signature));
            }

            return valid;

        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    /**
     * HMAC-SHA256 서명 계산
     */
    private String computeSignature(String body) throws Exception {
        String secretKey = paymentProperties.getToss().getSecretKey();

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 서명 마스킹 (로그용)
     */
    private String maskSignature(String signature) {
        if (signature == null || signature.length() < 8) return "***";
        return signature.substring(0, 4) + "****" + signature.substring(signature.length() - 4);
    }
}

