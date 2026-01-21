package com.back.global.geo.service;

import com.back.global.geo.entity.AddressDto;
import com.back.global.geo.entity.GeoApiResponseDto;

public class GeoApiService {
    private final String apiUrl = "https://dapi.kakao.com/v2/local/search/address";

    // 카카오 Local API
    public String getGeoCode(AddressDto addressDto) {
        // header Authorization: KakaoAK ${REST_API_KEY}

        GeoApiResponseDto responseDto;
        // bcode  = addresDto.getBCode();
        // responseDto.documents(filter address bcode)

        return null;
    }
}
