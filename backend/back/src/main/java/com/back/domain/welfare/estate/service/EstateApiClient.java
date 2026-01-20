package com.back.domain.welfare.estate.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstateApiClient {
    private final RestTemplate restTemplate;

    @Value("${custom.api.estate.url}")
    String apiUrl;

    @Value("${custom.api.estate.key}")
    String apiKey;

    // 국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API
    public EstateFetchResponseDto fetchEstatePage(EstateFetchRequestDto requestDto, int pageSize, int pageNo) {

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", apiKey)
                .queryParam("numOfRows", String.valueOf(pageSize))
                .queryParam("pageNo", String.valueOf(pageNo))
                .build(true)
                .toUri();

        Optional<EstateFetchResponseDto> responseDto =
                Optional.ofNullable(restTemplate.getForObject(uri, EstateFetchResponseDto.class));

        return responseDto.orElseThrow(() -> new RuntimeException(""));
    }
}
