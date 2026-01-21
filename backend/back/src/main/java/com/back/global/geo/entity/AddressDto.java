package com.back.global.geo.entity;

public record AddressDto(
        // 카카오 우편번호 검색 API 제공
        String postcode, // 우편번호
        String addressName, // 전체 주소
        String sigunguCode, // 41135 시/군/구 코드
        String bCode, // 4113511000	법정동/법정리 코드
        String roadAddress, // 도로명주소

        // 카카오 Local API 제공
        // 도로명 주소로 가져온다.
        String hCode, // "4514069000" 행정동 코드
        Double latitude, // 위도
        Double longitude // 경도
        ) {}
