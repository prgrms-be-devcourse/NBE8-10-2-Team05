package com.back.global.springBatch;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchStepFactory {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AsyncTaskExecutor taskExecutor;
    private final RetryPolicy retryPolicy;

    public <I, O> Step createApiStep(
            String stepName, ItemReader<I> reader, ItemProcessor<I, O> processor, ItemWriter<O> writer) {

        return new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(1000)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .transactionManager(transactionManager)
                .faultTolerant()
                .retryPolicy(retryPolicy) // 공통으로 사용하는 RetryPolicy
                .taskExecutor(taskExecutor)
                .build();
    }
}
