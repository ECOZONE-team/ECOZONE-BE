package co.ecozone.ecozoneapi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * 전역 시간 구성
 * - java.time.Clock(UTC) 빈을 제공하여 시간 의존 로직의 테스트 용이성 확보
 * - 프로필/테스트에서 Clock 교체 가능(Clock.fixed 등)
 * @since 2025-08-23
 * @author jeongdayeon
 */
@Configuration
public class TimeConfig {

    /** 시스템 전역에서 사용할 표준 시계 (UTC 권장) */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }

    @Bean
    @Profile("kst")
    public Clock kstClock() {
        return Clock.system(java.time.ZoneId.of("Asia/Seoul"));
    }

}
