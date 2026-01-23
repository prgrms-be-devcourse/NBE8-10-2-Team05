package com.back.domain.welfare.center.center.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CenterResponseDto(
        int page, int perPage, int totalCount, int currentCount, int matchCount, List<CenterDto> data) {
    public record CenterDto(
            @JsonProperty("연번") int id,

            @JsonProperty("시도") String city,

            @JsonProperty("기관명") String facilityName,

            @JsonProperty("주소") String address,

            @JsonProperty("전화번호") String phoneNumber,

            @JsonProperty("운영주체") String operator,

            @JsonProperty("법인유형") String corporationType) {}
}
