package com.back.global.springBatch.policy;

import java.util.Arrays;

import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.policy.entity.Policy;
import com.back.global.springBatch.elasticSearch.PolicyEsItemWriter;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PolicyApiItemWriter {
    private final EntityManagerFactory entityManagerFactory;
    private final PolicyEsItemWriter policyEsItemWriter;

    @Bean
    public JpaItemWriter<Policy> policyJpaItemWriter() {

        return new JpaItemWriterBuilder<Policy>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    // 부분 실패: DB 저장은 성공했는데 ES 전송 중 네트워크 오류가 나면, Spring Batch는 해당 **Chunk 전체를 실패(Rollback)**로 처리합니다.
    @Bean
    public CompositeItemWriter<Policy> compositePolicyWriter() {
        return new CompositeItemWriterBuilder<Policy>()
                .delegates(Arrays.asList(policyJpaItemWriter(), policyEsItemWriter)) // DB -> ES 순서
                .build();
    }
}
