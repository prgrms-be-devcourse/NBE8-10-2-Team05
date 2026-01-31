package com.back.global.springBatch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BatchController {
    private final BatchJobLauncher batchJobLauncher;

    @GetMapping("/batchTest")
    public void setup() {
        batchJobLauncher.runJob();
    }
}
