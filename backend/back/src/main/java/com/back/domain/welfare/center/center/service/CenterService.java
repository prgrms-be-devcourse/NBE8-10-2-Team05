package com.back.domain.welfare.center.center.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.back.domain.welfare.center.center.dto.CenterRequestDto;
import com.back.domain.welfare.center.center.dto.CenterResponseDto;
import com.back.domain.welfare.center.center.repository.CenterRepository;
import com.back.domain.welfare.center.entity.Center;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CenterService {
    private final CenterApiService centerApiService;
    private final CenterRepository centerRepository;

    public String getCenterData(CenterRequestDto centerRequestDto) {
        CenterResponseDto responseDto = centerApiService.fetchCenter(centerRequestDto);
        List<Center> centerList =
                responseDto.data().stream().map(Center::dtoToEntity).toList();
        centerRepository.saveAll(centerList);

        return null;
    }
}
