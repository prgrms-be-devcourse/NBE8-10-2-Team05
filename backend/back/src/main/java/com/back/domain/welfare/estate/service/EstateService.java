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

    public EstateFetchResponseDto fetchEstateList(EstateFetchRequestDto requestDto) {

        // api가 한번에 모든 정보를 갖고오도록
        EstateFetchResponseDto responseDto = fetchEstatePage(requestDto, 100, 1);
        int totalCnt = Integer.parseInt(responseDto.response().body().totalCount());
        if (totalCnt > 100) {
            // totalCnt가 너무 커서 서버 용량이 오버되는 경우 고려해야 함.
            responseDto = fetchEstatePage(requestDto, totalCnt, 1);
        }

        saveEstateList(responseDto);

        return responseDto;
    }

    public List<Estate> saveEstateList(EstateFetchResponseDto responseDto) {
        List<Estate> estateList =
                responseDto.response().body().items().stream().map(Estate::new).toList();

        return estateRepository.saveAll(estateList);
    }

    // 국토교통부_마이홈포털 공공주택 모집공고 조회 서비스 API
    public EstateFetchResponseDto fetchEstatePage(EstateFetchRequestDto requestDto, int pageSize, int pageNo) {

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("serviceKey", apiKey)
                .queryParam("numOfRows", String.valueOf(pageSize))
                .queryParam("pageNo", String.valueOf(pageNo))
                .build(true)
                .toUri();

        Optional<EstateFetchResponseDto> responseDto =
                Optional.ofNullable(new RestTemplate().getForObject(uri, EstateFetchResponseDto.class));

        return responseDto.orElseThrow(() -> new RuntimeException(""));
    }
}
