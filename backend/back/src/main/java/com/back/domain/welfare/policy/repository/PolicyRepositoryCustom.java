package com.back.domain.welfare.policy.repository;

import java.util.List;

import com.back.domain.welfare.policy.dto.PolicyRequestDto;
import com.back.domain.welfare.policy.dto.PolicyResponseDto;

public interface PolicyRepositoryCustom {
    List<PolicyResponseDto> search(PolicyRequestDto condition);
}
