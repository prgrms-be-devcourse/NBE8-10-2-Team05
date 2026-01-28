package com.back.global.springBatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.back.domain.welfare.center.center.entity.Center;
import com.back.global.springBatch.center.CenterApiItemProcessor;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class BatchConfigTest {

    // test : contextLoads 에서 확인가능하듯이, 실제로는 정상적으로 주입받음에도
    // IDE는 오류로 판단하기때문에 @SuppressWarnings
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Test
    void contextLoads() {
        assertThat(jobRepositoryTestUtils).isNotNull();
        assertThat(jobOperatorTestUtils).isNotNull();
    }

    @MockitoBean
    CenterApiItemProcessor centerApiItemProcessor;

    @BeforeEach
    void clearMetadata() {
        jobRepositoryTestUtils.removeJobExecutions(); // 이전 테스트 기록 삭제
    }

    @Test
    void retryTest() throws Exception {
        // given: 2번 실패 후 3번째 성공하는 로직을 Mockito로 설정
        given(centerApiItemProcessor.process(any()))
                .willThrow(new SocketTimeoutException("1차 실패"))
                .willThrow(new SocketTimeoutException("2차 실패"))
                .willReturn(new Center()); // 3차 성공

        // when
        JobExecution jobExecution = jobOperatorTestUtils.startStep("fetchCenterApiStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        // BATCH_STEP_EXECUTION 테이블의 rollback_count가 2인지 확인 (재시도 횟수)
        assertThat(jobExecution.getStepExecutions().iterator().next().getRollbackCount())
                .isEqualTo(2);
    }

    @Test
    void multiThreadTest() throws Exception {
        // given: 스레드 이름을 저장할 동기화된 셋
        Set<String> threadNames = Collections.synchronizedSet(new HashSet<>());

        doAnswer(invocation -> {
                    threadNames.add(Thread.currentThread().getName());
                    return new Center();
                })
                .when(centerApiItemProcessor)
                .process(any());

        // when
        jobOperatorTestUtils.startStep("fetchCenterApiStep");

        // then: 사용된 스레드 이름이 1개 이상(멀티스레드)인지 확인
        assertThat(threadNames.size()).isGreaterThan(1);
        System.out.println("사용한 스레드들: " + threadNames);
    }
}
