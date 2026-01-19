package com.back.domain.welfare.estate.service;

import java.util.List;
import java.util.Optional;

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

    public List<EstateFetchResponseDto> fetchEstateList(EstateFetchRequestDto requestDto) {
        // api 받아오기
        String apiUrl = "http://apis.data.go.kr/1613000/HWSPR02/rsdtRcritNtcList";
        String apiKey = "";

        String uri = UriComponentsBuilder.fromUriString("http://apis.data.go.kr/...")
                .queryParam("serviceKey", apiKey)
                .queryParam("brtcCode", requestDto.brtcCode())
                .build(false) // 공공데이터는 인코딩 방지를 위해 false를 쓰기도 함
                .toUriString();

        Optional<EstateFetchResponseDto> responseDto =
                Optional.ofNullable(new RestTemplate().getForObject(uri, EstateFetchResponseDto.class));

        // repository.save(EstateList)

        return List.of();
    }
}
