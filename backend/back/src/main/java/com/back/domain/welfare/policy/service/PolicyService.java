package com.back.domain.welfare.policy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.policy.dto.PolicySearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto;
import com.back.domain.welfare.policy.repository.PolicyRepositoryCustom;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {
    private final PolicyRepositoryCustom policyRepository;

    public List<PolicySearchResponseDto> search(PolicySearchRequestDto policySearchRequestDto) {
        return policyRepository.search(policySearchRequestDto);
    }
}
