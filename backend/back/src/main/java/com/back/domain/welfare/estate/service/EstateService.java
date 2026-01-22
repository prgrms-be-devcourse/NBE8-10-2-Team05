package com.back.domain.welfare.estate.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.estate.dto.EstateFetchRequestDto;
import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.repository.EstateRepository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstateService {
    private final EstateRepository estateRepository;
    private final EstateApiClient estateApiClient;

    @SneakyThrows
    public List<Estate> fetchEstateList(EstateFetchRequestDto requestDto) {
        // api가 한번에 모든 정보를 갖고오도록
        int pageSize = 100;
        EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto, pageSize, 1);
        int totalCnt = Integer.parseInt(responseDto.response().body().totalCount());
        int totalPages = (int) Math.ceil((double) totalCnt / pageSize);

        // 대부분 100개 안에서 해결될 것이라 가정
        List<Estate> estateList = new ArrayList<>(estateListFromResponse(responseDto));

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {

            EstateFetchResponseDto nextResponseDto = estateApiClient.fetchEstatePage(requestDto, pageSize, pageNo);
            estateList.addAll(estateListFromResponse(nextResponseDto));
            Thread.sleep(500);
        }

        return estateRepository.saveAll(estateList);
    }

    @Transactional
    public List<Estate> saveEstateList(EstateFetchResponseDto responseDto) {
        return estateRepository.saveAll(estateListFromResponse(responseDto));
    }

    private List<Estate> estateListFromResponse(EstateFetchResponseDto responseDto) {
        EstateFetchResponseDto.Response.BodyDto body = responseDto.response().body();
        if (body.items() == null || body.items().isEmpty()) {
            return List.of();
        }
        return body.items().stream().map(Estate::new).toList();
    }
}
