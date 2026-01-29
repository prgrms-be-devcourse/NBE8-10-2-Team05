package com.back.global.springBatch.center;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.center.center.entity.Center;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CenterApiItemWriter {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaItemWriter<Center> centerJpaItemWriter() {
        return new JpaItemWriterBuilder<Center>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
