package com.back.global.springBatch.center;

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
    public JpaItemWriter<Center> jpaItemWriter() {
        return new JpaItemWriterBuilder<Center>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
