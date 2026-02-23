package com.back.domain.welfare.policy.repository;

import java.util.List;

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto;

public interface PolicyRepositoryCustom {
    List<PolicySearchResponseDto> search(PolicySearchRequestDto condition);
}
