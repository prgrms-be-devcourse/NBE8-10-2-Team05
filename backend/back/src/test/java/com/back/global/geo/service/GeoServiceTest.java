package com.back.global.geo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.geo.entity.AddressDto;
import com.back.domain.member.geo.entity.GeoApiResponseDto;
import com.back.domain.member.geo.service.GeoApiService;
import com.back.domain.member.geo.service.GeoService;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GeoServiceTest {
    @MockitoBean
    private GeoApiService geoApiService;

    @Autowired
    private GeoService geoService;

    @Test
    @DisplayName("getGeoCode 테스트")
    void t1() {

        AddressDto addressDto = AddressDto.builder().addressName("경기도 성남시 분당구").build();

        AddressDto updatedAddressDto = AddressDto.builder()
                .hCode("4113500000") // 업데이트될 값
                .latitude(37.3947)
                .longitude(127.1111)
                .build();
        GeoApiResponseDto responseDto = mockResponse();

        when(geoApiService.fetchGeoCode(any())).thenReturn(responseDto);

        AddressDto result = geoService.getGeoCode(addressDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.hCode()).isEqualTo("4113500000");
        assertThat(result.latitude()).isEqualTo(37.3947);
        assertThat(result.longitude()).isEqualTo(127.1111);

        // repository의 search 메서드가 정확히 1번 호출되었는지 검증
        verify(geoApiService, times(1)).fetchGeoCode(addressDto);
    }

    private GeoApiResponseDto mockResponse() {
        GeoApiResponseDto.Address address = new GeoApiResponseDto.Address(
                "경기도 성남시 분당구 분당로 50", // addressName
                "경기", // region1depthName
                "성남시 분당구", // region2depthName
                "수내동", // region3depthName
                "4113500000", // hCode (행정동 코드)
                "4113510300", // bCode (법정동 코드)
                "127.1111", // x (경도)
                "37.3947" // y (위도)
                );

        GeoApiResponseDto.RoadAddress roadAddress =
                new GeoApiResponseDto.RoadAddress("경기도 성남시 분당구 분당로 50", "13590", "127.1111", "37.3947");

        GeoApiResponseDto.Document document = new GeoApiResponseDto.Document(
                "경기도 성남시 분당구 분당로 50", "127.1111", "37.3947", "REGION_ADDR", address, roadAddress);

        GeoApiResponseDto.Meta meta = new GeoApiResponseDto.Meta(1, 1, true);

        return new GeoApiResponseDto(meta, List.of(document));
    }
}
