package com.back.domain.welfare.center.center.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.welfare.center.center.config.CenterApiProperties;
import com.back.domain.welfare.center.center.dto.CenterRequestDto;
import com.back.domain.welfare.center.center.dto.CenterResponseDto;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CenterApiService {
    private final WebClient webClient = WebClient.builder().build();
    private final CenterApiProperties centerApiProperties;

    // 보건복지부_사회복지관 시도별 주소 현황 공공 API
    public CenterResponseDto fetchCenter(CenterRequestDto centerRequestDto) {

        String requestUrl = centerApiProperties.url()
                + "?page=" + centerRequestDto.page()
                + "&perPage=" + centerRequestDto.perPage()
                + "&serviceKey=" + centerApiProperties.key();

        CenterResponseDto responseDto = Optional.ofNullable(webClient
                        .get()
                        .uri(URI.create(requestUrl))
                        .retrieve()
                        .bodyToMono(CenterResponseDto.class)
                        .block())
                .orElseThrow(() -> new ServiceException("501", "center api 데이터를 가져오는데 실패하였습니다."));

        return responseDto;
    }
}
