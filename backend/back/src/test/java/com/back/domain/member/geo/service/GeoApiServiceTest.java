package com.back.domain.member.geo.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.geo.dto.GeoApiResponseDto;
import com.back.domain.member.geo.entity.AddressDto;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GeoApiServiceTest {

    @Autowired
    private GeoApiService geoApiService;

    @Test
    @DisplayName("실제 카카오 local API 테스트")
    @Disabled
    void t0() {
        AddressDto addressDto =
                AddressDto.builder().roadAddress("경기도 성남시 분당구 분당로 50").build();
        GeoApiResponseDto result = geoApiService.fetchGeoCode(addressDto);

        assertThat(result).isNotNull();
        assertThat(result.documents()).isNotNull();
        assertThat(result.documents()).isNotEmpty();
        assertThat(result.documents().getFirst().address()).isNotNull();

        GeoApiResponseDto.Address resultAddress = result.documents().getFirst().address();

        assertThat(resultAddress.hCode()).isNotNull();
        assertThat(resultAddress.hCode()).isNotEmpty();
        assertThat(resultAddress.x()).isNotNull();
        assertThat(resultAddress.y()).isNotNull();
    }
}
