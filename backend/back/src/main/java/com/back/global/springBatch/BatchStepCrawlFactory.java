package com.back.global.springBatch;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchStepCrawlFactory {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AsyncTaskExecutor crawlingTaskExecutor;

    public <I, O> Step createCrawlStep(
            String stepName, ItemReader<I> reader, ItemProcessor<I, O> processor, ItemWriter<O> writer) {

        return new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(1000)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .transactionManager(transactionManager)
                .taskExecutor(crawlingTaskExecutor)
                .build();
    }
}
