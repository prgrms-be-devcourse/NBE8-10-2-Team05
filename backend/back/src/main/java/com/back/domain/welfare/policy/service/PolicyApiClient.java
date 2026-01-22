package com.back.domain.welfare.policy.service;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.policy.config.YouthPolicyProperties;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyApiClient {

    private final YouthPolicyProperties properties;
    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper; // Bean 주입

    /**
     * API에서 한 페이지 가져오기
     */
    public PolicyFetchResponseDto fetchPolicyPage(PolicyFetchRequestDto requestDto, int pageNum, int pageSize) {
        try {
            String requestUrl = properties.url()
                    + "?apiKeyNm=" + properties.key()
                    + "&pageType=" + requestDto.pageType()
                    + "&pageSize=" + pageSize
                    + "&pageNo=" + pageNum
                    + "&rtnType=" + requestDto.rtnType();

            String response = webClient
                    .get()
                    .uri(URI.create(requestUrl))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Policy API 응답이 null입니다.");
            }

            // JSON → DTO
            return objectMapper.readValue(response, PolicyFetchResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Policy API 호출 실패", e);
        }
    }
}
