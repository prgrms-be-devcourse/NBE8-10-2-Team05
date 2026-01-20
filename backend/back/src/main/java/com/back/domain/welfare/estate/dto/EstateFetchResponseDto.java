package com.back.domain.welfare.estate.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstateFetchResponseDto(
        @JsonProperty("response") Response response) {
    public record Response(
            @JsonProperty("header") HeaderDto header,
            @JsonProperty("body") ResponseDto body) {
        public record HeaderDto(
                @JsonProperty("resultCode") String resultCode,
                @JsonProperty("resultMsg") String resultMsg) {}

        public record ResponseDto(
                @JsonProperty("numOfRows") String numOfRows,
                @JsonProperty("pageNo") String pageNo,
                @JsonProperty("totalCount") String totalCount,
                @JsonProperty("item") List<EstateItem> items) {}
    }
}
