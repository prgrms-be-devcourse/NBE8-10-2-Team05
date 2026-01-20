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
    public List<Estate> fetchEstateList(EstateFetchRequestDto requestDto) {

        // api가 한번에 모든 정보를 갖고오도록
        int pageSize = 100;
        EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto, pageSize, 1);
        int totalCnt = Integer.parseInt(responseDto.response().body().totalCount());
        int totalPages = (int) Math.ceil((double) totalCnt / pageSize);

        // 대부분 100개 안에서 해결될 것이라 가정
        List<Estate> estateList = saveEstateList(responseDto);

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            EstateFetchResponseDto nextResponseDto = estateApiClient.fetchEstatePage(requestDto, pageSize, pageNo);
            estateList.addAll(saveEstateList(nextResponseDto));
            // sleep()
        }

        return estateList;
    }

    @Transactional
    public List<Estate> saveEstateList(EstateFetchResponseDto responseDto) {
        List<Estate> estateList =
                responseDto.response().body().items().stream().map(Estate::new).toList();

        return estateRepository.saveAll(estateList);
    }
}
