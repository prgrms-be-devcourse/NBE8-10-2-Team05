package com.back.global.springBatch.policy;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.back.domain.welfare.policy.entity.Policy;

@Component
@StepScope
public class PolicyApiItemProcessor implements ItemProcessor<PolicyFetchResponseDto.PolicyItem, Policy> {
    @Override
    public Policy process(PolicyFetchResponseDto.PolicyItem policyItem) throws Exception {
        return Policy.from(policyItem, "");
    }
}
