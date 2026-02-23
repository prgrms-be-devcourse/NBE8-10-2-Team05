package com.back.domain.welfare.policy.dto;

public record PolicyFetchRequestDto(
        String apiKeyNm,
        String pageType, // 1:목록, 2:상세
        String pageSize,
        String rtnType // xml인지 json인지
        ) {}
