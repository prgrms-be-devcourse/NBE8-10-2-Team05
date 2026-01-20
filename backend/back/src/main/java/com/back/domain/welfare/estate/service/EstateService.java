package com.back.domain.welfare.estate.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.repository.EstateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstateService {
    private final EstateRepository estateRepository;

    @Value("${custom.api.estate.url}")
    String apiUrl;

    @Value("${custom.api.estate.key}")
    String apiKey;

    // 국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API
    public EstateFetchResponseDto fetchEstateList(EstateFetchRequestDto requestDto) {

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", apiKey)
                .build(true)
                .toUri();

        Optional<EstateFetchResponseDto> responseDto =
                Optional.ofNullable(new RestTemplate().getForObject(uri, EstateFetchResponseDto.class));

        return responseDto.orElseThrow(() -> new RuntimeException(""));
    }

    public List<Estate> saveEstateList(EstateFetchResponseDto responseDto) {
        List<Estate> estateList =
                responseDto.response().body().items().stream().map(Estate::new).toList();

        return estateRepository.saveAll(estateList);
    }
}
