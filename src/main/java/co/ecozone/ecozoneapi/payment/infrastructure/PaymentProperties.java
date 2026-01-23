package co.ecozone.ecozoneapi.payment.infrastructure;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 결제 설정 프로퍼티 바인딩
 * - payment.toss.client_key / secret_key / security_key / base-url
 * - payment.ledger.* (원장 설정)
 * - 코드 하드코딩 제거 및 프로파일/환경변수 주입
 * @since 2025-09-16
 */
@Data
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    @Data
    public static class Toss {
        @NotBlank
        private String clientKey;

        @NotBlank
        private String secretKey;

        private String securityKey;

        private String baseUrl = "https://api.tosspayments.com";
    }

    @Data
    public static class Ledger {
        private String userAccountPrefix = "USER:";
        private String merchantAccountPrefix = "MERCHANT:";
        private String merchantId = "eco";
        private String cancelKeyPrefix = "cancel:";
        private String confirmMemo = "confirm";
        private String cancelMemoPrefix = "cancel:";
    }

    private Toss toss = new Toss();
    private Ledger ledger = new Ledger();
}
