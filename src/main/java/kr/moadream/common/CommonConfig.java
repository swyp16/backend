package kr.moadream.common;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 현재 시각은 이 Clock 빈으로만 얻는다. 시계는 마이크로초 단위다. 테스트에서 시각을 고정하려면 Clock 빈을 바꾼다.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class CommonConfig {

    /** 날짜 계산(마감일 등)은 이 시간대로 한다. 저장과 응답은 UTC Instant 다. */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Bean
    Clock clock() {
        return Clock.tick(Clock.systemUTC(), Duration.ofNanos(1_000));
    }

    @Bean
    DateTimeProvider auditingDateTimeProvider(Clock clock) {
        return () -> Optional.of(Instant.now(clock));
    }
}
