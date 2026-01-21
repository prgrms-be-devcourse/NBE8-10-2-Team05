package com.back.domain.welfare.policy.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.policy.dto.PolicyRequestDto;
import com.back.domain.welfare.policy.dto.PolicyResponseDto;
import com.back.domain.welfare.policy.service.PolicyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/policy")
@RequiredArgsConstructor
public class PolicyController {
    PolicyService policyService;

    @GetMapping("/search")
    public List<PolicyResponseDto> search(PolicyRequestDto policyRequestDto) {
        return policyService.search(policyRequestDto);
    }
}
