package com.back.global.springBatch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BatchController {
    private final BatchJobLauncher batchJobLauncher;

    @GetMapping("/batchTest")
    public String setup() {
        log.info(">>> 사용자 요청: 배치 프로세스 가동");

        // 비동기로 실행 (배치 실행 메서드에 @Async가 붙어있어야 함)
        batchJobLauncher.runJob();

        // 브라우저에는 즉시 응답 반환
        return "배치(API & Crawling)가 백그라운드에서 순차적으로 시작되었습니다. 로그를 확인하세요!";
    }
}
