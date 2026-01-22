package com.back.domain.welfare.estate.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.estate.config.EstateConfigProperties;
import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstateApiClient {
    private final WebClient webClient = WebClient.builder().build();
    private final EstateConfigProperties estateConfigProperties;

    // 국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API
    public EstateFetchResponseDto fetchEstatePage(EstateFetchRequestDto requestDto, int pageSize, int pageNo) {
        log.debug("fetchEstatePage(%d,%d,%d) 실행".formatted(requestDto, pageSize, pageNo));

        String requestUrl = estateConfigProperties.url();

        EstateFetchResponseDto responseDto = Optional.ofNullable(webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(requestUrl)
                                .queryParam("serviceKey", estateConfigProperties.key())
                                .queryParam("numOfRows", String.valueOf(pageSize))
                                .queryParam("pageNo", String.valueOf(pageNo))
                                .build())
                        .retrieve()
                        .bodyToMono(EstateFetchResponseDto.class)
                        .block())
                .orElseThrow(() -> new ServiceException("501", "API호출과정에 에러가 생겼습니다."));

        return responseDto;
    }
}
