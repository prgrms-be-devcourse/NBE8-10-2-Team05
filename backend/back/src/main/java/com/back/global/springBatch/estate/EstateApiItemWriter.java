package com.back.global.springBatch.estate;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.estate.entity.Estate;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EstateApiItemWriter {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaItemWriter<Estate> jpaItemWriter() {
        return new JpaItemWriterBuilder<Estate>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
