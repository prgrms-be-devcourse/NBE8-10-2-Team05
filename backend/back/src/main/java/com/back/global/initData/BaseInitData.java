package com.back.global.initData;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.service.EstateApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BaseInitData {
    private final EstateApiClient estateApiClient;

    @Bean
    public ApplicationRunner initEstateData() {
        return args -> {
            EstateFetchRequestDto requestDto =
                    EstateFetchRequestDto.builder().numOfRows(10).pageNo(1).build();
            EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto);
            log.debug("실제 API 잘 받아지는지 테스트 {}", responseDto);
        };
    }
}
