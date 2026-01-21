package com.back.global.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SyncScheduler {

    @Scheduled(cron = "0 30 09 * * *")
    public void runDailyCrawling() {
        log.debug("SyncScheduler : runDailyCrawling 실행");
    }

    @Scheduled(cron = "0 30 09 1 * *")
    public void runMonthlyCrawling() {
        log.debug("SyncScheduler : runMonthlyCrawling 실행");
    }
}
