package com.mapic.backend.scheduler;

import com.mapic.backend.service.IFriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestCleanupScheduler {

    private final IFriendService friendService;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredRequests() {
        log.info("Starting cleanup of expired friend requests");
        friendService.cleanupExpiredRequests();
        log.info("Completed cleanup of expired friend requests");
    }
}
