package co.ecozone.ecozoneapi.payment.application.config;

import co.ecozone.ecozoneapi.payment.infrastructure.PaymentProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * 결제 클라이언트 구성 
 * - RestClient 빈 등록: baseUrl/BasicAuth(secret_key,"")
 * - 네트워크 설정/타임아웃/인증 헤더 공통화로 호출부 단순화
 * @since 2025-09-16
 */
@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentClientConfig {

    @Bean("tossRestClient")
    public RestClient tossRestClient(RestClient.Builder builder, PaymentProperties props) {
        var jdk = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return builder
                .baseUrl(props.getToss().getBaseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(jdk))
                .defaultHeaders(h -> h.setBasicAuth(props.getToss().getSecretKey(), ""))
                .build();
    }
}