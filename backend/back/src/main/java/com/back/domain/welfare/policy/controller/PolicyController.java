package com.back.domain.welfare.policy.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto;
import com.back.domain.welfare.policy.service.PolicyFetchService;
import com.back.domain.welfare.policy.service.PolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/policy")
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;
    private final PolicyFetchService policyFetchService;

    @GetMapping("/search")
    public List<PolicySearchResponseDto> search(PolicySearchRequestDto policySearchRequestDto) {
        return policyService.search(policySearchRequestDto);
    }

    @GetMapping("/list")
    public void getPolicy() {
        PolicyFetchRequestDto requestDto = new PolicyFetchRequestDto(null, "1", "100", "json");

        policyFetchService.fetchAndSavePolicies(requestDto);
    }
}
