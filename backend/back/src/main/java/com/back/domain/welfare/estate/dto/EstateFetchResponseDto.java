package com.back.domain.welfare.estate.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstateFetchResponseDto(
        @JsonProperty("numOfRows") String numOfRows,
        @JsonProperty("pageNo") String pageNo,
        @JsonProperty("totalCount") String totalCount,
        @JsonProperty("item") List<EstateItem> items) {}
