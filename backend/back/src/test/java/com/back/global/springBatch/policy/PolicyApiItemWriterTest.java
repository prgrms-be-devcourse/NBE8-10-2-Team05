package com.back.global.springBatch.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.back.domain.welfare.policy.repository.PolicyRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@SpringBatchTest
@SpringBootTest
class PolicyApiItemWriterTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private PolicyRepository policyRepository; // JPA 확인용

    @Autowired
    private ElasticsearchClient esClient; // ES 확인용

    @Test
    @DisplayName("batch시 실제 es동기화가 진행되는지")
    void compositeWriter() throws Exception {
        // when
        JobExecution jobExecution = jobOperatorTestUtils.startJob();

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DB에 저장되었는지 확인
        long dbCount = policyRepository.count();
        assertThat(dbCount).isGreaterThan(0);

        esClient.indices().refresh(r -> r.index("policy_index"));

        // ES에 저장되었는지 확인 (동기화 시간이 필요할 수 있으므로 잠시 대기하거나 refresh 필요)
        var response = esClient.count(c -> c.index("policy_index"));
        assertThat(response.count()).isEqualTo(dbCount);
    }

    // rollback test

}
