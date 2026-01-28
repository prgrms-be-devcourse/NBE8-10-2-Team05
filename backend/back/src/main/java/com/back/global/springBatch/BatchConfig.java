package com.back.global.springBatch;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Set;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.ResourceAccessException;

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.global.springBatch.center.CenterApiItemProcessor;
import com.back.global.springBatch.center.CenterApiItemReader;
import com.back.global.springBatch.center.CenterApiItemWriter;

import io.netty.channel.ConnectTimeoutException;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final CenterApiItemReader centerApiItemReader;
    private final CenterApiItemProcessor centerApiItemProcessor;
    private final CenterApiItemWriter centerApiItemWriter;

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8); // 기본 스레드 수 mysql-hikari의 기본설명은 10개
        executor.setMaxPoolSize(8); // 최대 스레드 수 일이 너무 많아져서 대기 큐(Queue)까지 꽉 차면, 일꾼을 추가로 더 뽑습니다
        // executor.setQueueCapacity(50);    // 대기 큐 기본 일꾼 10명이 모두 일하고 있다면, 새로 들어온 작업(청크 단위)은 이 대기 줄에 서서 기다립니다. 50개가 넘게
        // 줄을 서면 그때서야 MaxPoolSize만큼 일꾼을 추가로 투입
        executor.setThreadNamePrefix("batch-thread-"); // 로그를 찍을 때 [batch-thread-1], [batch-thread-2] 처럼 출력
        executor.initialize();
        return executor;
    }

    @Bean
    public RetryPolicy retryPolicy() {
        return RetryPolicy.builder()
                .maxRetries(4) // 최초 1 + 재시도 3번
                .delay(Duration.ofSeconds(2)) // 2초 대기
                .includes(Set.of(
                        SocketTimeoutException.class, ResourceAccessException.class, ConnectTimeoutException.class))
                .build();
    }

    @Bean
    public Job fetchApiJob(
            JobRepository jobRepository, Step fetchCenterApiStep, Step fetchEstateApiStep, Step fetchPolicyApiStep) {

        return new JobBuilder("fetchApiJob", jobRepository)
                .start(fetchCenterApiStep)
                .build();
    }

    @Bean
    public Step fetchCenterApiStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AsyncTaskExecutor taskExecutor) {

        return new StepBuilder("fetchCenterApiStep", jobRepository)
                .<CenterApiResponseDto.CenterDto, Center>chunk(1000)
                .reader(centerApiItemReader) // @Value로 주입받기 위해 null 전달
                .processor(centerApiItemProcessor)
                .writer(centerApiItemWriter.jpaItemWriter())
                .transactionManager(transactionManager)
                .faultTolerant() // retry나 skip 같은 예외 처리 옵션들을 사용 스위치
                .retryPolicy(retryPolicy())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step fetchEstateApiStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fetchEstateApiStep", jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            System.out.println("Hello, Spring Batch!");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }

    @Bean
    public Step fetchPolicyApiStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fetchPolicyApiStep", jobRepository)
                .tasklet(
                        (contribution, chunkContext) -> {
                            System.out.println("Hello, Spring Batch!");
                            return RepeatStatus.FINISHED;
                        },
                        transactionManager)
                .build();
    }

    // exceptionClass.put(ConnectException.class, true);
    //        exceptionClass.put(SocketTimeoutException.class, true);
    //        exceptionClass.put(RestClientException.class, true);

}
