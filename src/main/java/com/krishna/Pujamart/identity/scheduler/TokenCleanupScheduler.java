package com.krishna.Pujamart.identity.scheduler;

import com.krishna.Pujamart.identity.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
@AllArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    @Scheduled(cron = "0 0 0 * * ?")
    @SchedulerLock(
            name = "TokenCleanupScheduler_cleanExpiredTokens",
            lockAtMostFor = "15m",
            lockAtLeastFor = "5m"
    )
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }
}

