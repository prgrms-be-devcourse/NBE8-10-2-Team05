package com.back.global.geo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.back.global.exception.ServiceException;
import com.back.global.geo.config.GeoApiProperties;
import com.back.global.geo.entity.AddressDto;
import com.back.global.geo.entity.GeoApiResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoApiService {
    private final WebClient webClient = WebClient.builder().build();
    private final GeoApiProperties geoApiProperties;

    // 카카오 Local API
    public AddressDto fetchGeoCode(AddressDto addressDto) {
        // header Authorization: KakaoAK ${REST_API_KEY}
        // bcode  = addresDto.getBCode();
        // responseDto.documents(filter address bcode)

        //        curl -v -G GET "https://dapi.kakao.com/v2/local/search/address.json" \
        //        -H "Authorization: KakaoAK ${REST_API_KEY}" \
        //        --data-urlencode "query=전북 삼성동 100"

        String requestUrl = geoApiProperties.url() + "?query=" + addressDto.roadAddress();

        GeoApiResponseDto responseDto = webClient
                .get()
                .uri(requestUrl)
                .header("Authorization", "KakaoAK " + geoApiProperties.key())
                .retrieve()
                .bodyToMono(GeoApiResponseDto.class)
                .block();

        AddressDto updatedDto = Optional.ofNullable(responseDto)
                .map(GeoApiResponseDto::documents)
                .filter(docs -> !docs.isEmpty()) // 리스트가 비어있지 않은지 확인
                .map(docs -> docs.get(0)) // 첫 번째 Document 꺼내기
                .map(GeoApiResponseDto.Document::address)
                .map(address -> AddressDto.of(addressDto, address))
                .orElseThrow(() -> new ServiceException("501", "kakao local api return값에서 parsing에 실패하였습니다."));

        return updatedDto;
    }
}
