package com.back.domain.welfare.estate.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
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

    public EstateFetchResponseDto fetchEstateList(EstateFetchRequestDto requestDto) {

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", apiKey)
                .build(true)
                .toUri();

        Optional<EstateFetchResponseDto> responseDto =
                Optional.ofNullable(new RestTemplate().getForObject(uri, EstateFetchResponseDto.class));

        //
        // repository.save(EstateList)
        // List<Estate>

        return responseDto.orElseThrow(() -> new RuntimeException(""));
    }
}
