package com.back.domain.welfare.policy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.policy.config.YouthPolicyProperties;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final YouthPolicyProperties properties;

    private final WebClient webClient = WebClient.builder().build();

    public String fetchPolicies(PolicyFetchRequestDto requestDto) {

        String requestUrl = properties.url()
                + "?apiKeyNm=" + properties.key()
                + "&pageType=" + requestDto.pageType()
                + "&pageSize=" + requestDto.pageSize()
                + "&rtnType=" + requestDto.rtnType();

        return webClient
                .get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
