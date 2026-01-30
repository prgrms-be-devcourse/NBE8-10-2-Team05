package com.back.global.springBatch;

import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final BatchJobLauncher batchJobLauncher;

    @PostMapping("/start")
    public String startBatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        batchJobLauncher.runJob();

        stopWatch.stop();
        log.info("Spring Batch Job 실행 요청 완료. 총 소요 시간: {} ms", stopWatch.getTotalTimeMillis());

        return "Spring Batch Job 실행이 시작되었습니다. 백엔드 로그를 확인하세요.";
    }
}
