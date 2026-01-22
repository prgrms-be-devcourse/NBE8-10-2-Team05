package com.back.global.geo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.back.global.geo.entity.AddressDto;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GeoApiServiceTest {
    @MockitoBean
    private GeoApiService geoApiService;

    @Test
    @DisplayName("fetchGeoCode 테스트")
    void t1() {

        AddressDto addressDto = AddressDto.builder().addressName("경기도 성남시 분당구").build();

        AddressDto updatedAddressDto = AddressDto.builder()
                .hCode("4113500000") // 업데이트될 값
                .latitude(37.3947)
                .longitude(127.1111)
                .build();

        when(geoApiService.fetchGeoCode(any())).thenReturn(updatedAddressDto);

        AddressDto result = geoApiService.fetchGeoCode(addressDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.hCode()).isEqualTo("4113500000");
        assertThat(result.latitude()).isEqualTo(37.3947);

        // repository의 search 메서드가 정확히 1번 호출되었는지 검증
        verify(geoApiService, times(1)).fetchGeoCode(addressDto);
    }
}
