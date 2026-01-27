package com.back.domain.member.geo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.domain.member.geo.dto.GeoApiResponseDto;
import com.back.domain.member.geo.entity.AddressDto;
import com.back.domain.member.geo.properties.GeoApiProperties;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoApiService {
    private final WebClient webClient = WebClient.builder().build();
    private final GeoApiProperties geoApiProperties;

    // 카카오 Local API
    public GeoApiResponseDto fetchGeoCode(AddressDto addressDto) {
        String requestUrl = geoApiProperties.url() + "?query=" + addressDto.roadAddress();

        GeoApiResponseDto responseDto = Optional.ofNullable(webClient
                        .get()
                        .uri(requestUrl)
                        .header("Authorization", "KakaoAK " + geoApiProperties.key())
                        .retrieve()
                        .bodyToMono(GeoApiResponseDto.class)
                        .block())
                .orElseThrow(() -> new ServiceException("501", "kakao geo api 오류가 생겼습니다."));

        return responseDto;
    }
}
