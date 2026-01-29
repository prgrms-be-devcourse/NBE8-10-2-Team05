package com.back.global.springBatch;

import java.time.LocalDateTime;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BatchJobListener implements JobExecutionListener {
    @Override
    public void afterJob(JobExecution jobExecution) {
        // 종료 시간에서 시작 시간을 빼서 계산
        LocalDateTime start = jobExecution.getStartTime();
        LocalDateTime end = jobExecution.getEndTime();

        if (start != null && end != null) {
            long duration = java.time.Duration.between(start, end).toMillis();
            log.info(">>> [Job ID: {}] 최종 완료", jobExecution.getId());
            log.info(">>> 소요 시간: {}ms (약 {}초)", duration, duration / 1000.0);
            log.info(">>> 최종 상태: {}", jobExecution.getStatus());
        }
    }
}
