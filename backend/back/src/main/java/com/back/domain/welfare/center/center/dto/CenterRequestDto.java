package com.back.domain.welfare.center.center.dto;

public record CenterRequestDto(
        Integer page, // page index (default = 1)
        Integer perPage, // pageSize (default = 10)
        String returnType // json xml (default = json)
        ) {}
