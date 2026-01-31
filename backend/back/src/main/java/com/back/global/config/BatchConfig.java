package com.back.global.config;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.estate.dto.EstateDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.global.springBatch.BatchJobListener;
import com.back.global.springBatch.BatchStepCrawlFactory;
import com.back.global.springBatch.BatchStepFactory;
import com.back.global.springBatch.center.CenterApiItemProcessor;
import com.back.global.springBatch.center.CenterApiItemReader;
import com.back.global.springBatch.estate.EstateApiItemProcessor;
import com.back.global.springBatch.estate.EstateApiItemReader;
import com.back.global.springBatch.lawyer.LawyerApiItemReader;
import com.back.global.springBatch.policy.PolicyApiItemProcessor;
import com.back.global.springBatch.policy.PolicyApiItemReader;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableRetry
@RequiredArgsConstructor
public class BatchConfig {
    private final BatchJobListener batchJobListener;
    private final BatchStepFactory batchStepFactory;
    private final BatchStepCrawlFactory batchStepCrawlFactory;

    private final CenterApiItemReader centerApiItemReader;
    private final CenterApiItemProcessor centerApiItemProcessor;
    private final JpaItemWriter<Center> centerJpaItemWriter;

    private final EstateApiItemReader estateApiItemReader;
    private final EstateApiItemProcessor estateApiItemProcessor;
    private final JpaItemWriter<Estate> estateJpaItemWriter;

    private final PolicyApiItemReader policyApiItemReader;
    private final PolicyApiItemProcessor policyApiItemProcessor;
    // private final JpaItemWriter<Policy> policyJpaItemWriter;
    private final CompositeItemWriter<Policy> compositeItemWriter;

    private final LawyerApiItemReader lawyerApiItemReader;
    private final JpaItemWriter<Lawyer> lawyerJpaItemWriter;

    @Bean
    public Job fetchApiJob(
            JobRepository jobRepository,
            Step fetchCenterApiStep,
            Step fetchEstateApiStep,
            Step fetchPolicyApiStep,
            Step fetchLawyerApiStep) {

        return new JobBuilder("fetchApiJob", jobRepository)
                .listener(batchJobListener)
                // .start(fetchCenterApiStep)
                // .next(fetchEstateApiStep)
                // .next(fetchPolicyApiStep)
                .start(fetchLawyerApiStep)
                .build();
    }

    @Bean
    public Step fetchCenterApiStep(BatchStepFactory factory) {
        return factory.<CenterApiResponseDto.CenterDto, Center>createApiStep(
                "fetchCenterApiStep", centerApiItemReader, centerApiItemProcessor, centerJpaItemWriter);
    }

    @Bean
    public Step fetchEstateApiStep(BatchStepFactory factory) {
        return factory.<EstateDto, Estate>createApiStep(
                "fetchEstateApiStep", estateApiItemReader, estateApiItemProcessor, estateJpaItemWriter);
    }

    @Bean
    public Step fetchPolicyApiStep(BatchStepFactory factory) {
        return factory.<PolicyFetchResponseDto.PolicyItem, Policy>createApiStep(
                "fetchPolicyApiStep", policyApiItemReader, policyApiItemProcessor, compositeItemWriter);
    }

    @Bean
    public Step fetchLawyerApiStep(BatchStepCrawlFactory factory) {
        return factory.<Lawyer, Lawyer>createCrawlStep(
                "fetchLawyerApiStep", lawyerApiItemReader, null, lawyerJpaItemWriter);
    }
}
