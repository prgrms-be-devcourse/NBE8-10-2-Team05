package com.back.domain.member.geo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.back.domain.member.geo.entity.AddressDto;
import com.back.domain.member.geo.entity.GeoApiResponseDto;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoService {
    private final GeoApiService geoApiService;

    public AddressDto getGeoCode(AddressDto addressDto) {
        GeoApiResponseDto responseDto = geoApiService.fetchGeoCode(addressDto);

        AddressDto updatedDto = Optional.ofNullable(responseDto)
                .map(GeoApiResponseDto::documents)
                .filter(docs -> !docs.isEmpty()) // 리스트가 비어있지 않은지 확인
                .map(List::getFirst) // 첫 번째 Document 꺼내기
                .map(GeoApiResponseDto.Document::address)
                .map(address -> AddressDto.of(addressDto, address))
                .orElseThrow(() -> new ServiceException("501", "kakao local api return값에서 parsing에 실패하였습니다."));

        return updatedDto;
    }
}
