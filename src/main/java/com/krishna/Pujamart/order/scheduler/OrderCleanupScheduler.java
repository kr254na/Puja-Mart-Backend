package com.krishna.Pujamart.order.scheduler;

import com.krishna.Pujamart.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    private final OrderService orderService;
    // Runs every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    @SchedulerLock(
            name = "OrderCleanupScheduler_cleanupExpiredPendingOrders",
            lockAtMostFor = "10m",
            lockAtLeastFor = "2m"
    )
    public void cleanupExpiredPendingOrders() {
        log.info("Scheduled task started: cleaning up expired pending orders.");
        try {
            orderService.cancelExpiredPendingOrders();
            log.info("Scheduled task finished successfully.");
        } catch (Exception e) {
            log.error("Error occurred during expired pending orders cleanup task", e);
        }
    }
}

