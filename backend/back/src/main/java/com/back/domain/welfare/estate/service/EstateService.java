package com.back.domain.welfare.estate.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.repository.EstateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstateService {
    private final EstateRepository estateRepository;
    private final EstateApiClient estateApiClient;

    @Transactional
    public EstateFetchResponseDto fetchEstateList(EstateFetchRequestDto requestDto) {

        // api가 한번에 모든 정보를 갖고오도록
        EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto, 100, 1);
        int totalCnt = Integer.parseInt(responseDto.response().body().totalCount());
        if (totalCnt > 100) {
            // totalCnt가 너무 커서 서버 용량이 오버되는 경우 고려해야 함.
            responseDto = estateApiClient.fetchEstatePage(requestDto, totalCnt, 1);
        }

        saveEstateList(responseDto);

        return responseDto;
    }

    @Transactional
    public List<Estate> saveEstateList(EstateFetchResponseDto responseDto) {
        List<Estate> estateList =
                responseDto.response().body().items().stream().map(Estate::new).toList();

        return estateRepository.saveAll(estateList);
    }
}
