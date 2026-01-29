package com.back.global.springBatch.policy;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.policy.entity.Policy;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PolicyApiItemWriter {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaItemWriter<Policy> policyJpaItemWriter() {
        return new JpaItemWriterBuilder<Policy>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
