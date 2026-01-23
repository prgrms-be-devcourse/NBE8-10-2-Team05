package com.back.domain.welfare.center.center.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.back.domain.welfare.center.center.dto.CenterRequestDto;
import com.back.domain.welfare.center.center.dto.CenterResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.center.repository.CenterRepository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CenterService {
    private final CenterApiService centerApiService;
    private final CenterRepository centerRepository;

    @SneakyThrows
    public List<Center> getCenterData() {

        int pageSize = 100;
        CenterRequestDto centerRequestDto = CenterRequestDto.from(1, pageSize);
        CenterResponseDto responseDto = centerApiService.fetchCenter(centerRequestDto);

        int totalCnt = responseDto.totalCount();
        int totalPages = (int) Math.ceil((double) totalCnt / pageSize);

        List<Center> centerList = new ArrayList<>(
                responseDto.data().stream().map(Center::dtoToEntity).toList());

        centerRepository.saveAll(centerList);

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            log.debug("fetchCenter pageNo : {} ,pageSize : {} 실행", pageSize, pageNo);

            centerRequestDto = CenterRequestDto.from(pageNo, pageSize);
            CenterResponseDto nextResponseDto = centerApiService.fetchCenter(centerRequestDto);

            List<Center> updatedCenterList =
                    nextResponseDto.data().stream().map(Center::dtoToEntity).toList();

            centerRepository.saveAll(updatedCenterList);
            centerList.addAll(updatedCenterList);

            Thread.sleep(500);
        }

        return centerList;
    }
}
