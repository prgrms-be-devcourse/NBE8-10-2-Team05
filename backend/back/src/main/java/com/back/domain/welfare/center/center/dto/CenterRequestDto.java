package com.back.domain.welfare.center.center.dto;

public record CenterRequestDto(
        Integer page, // page index (default = 1)
        Integer perPage, // pageSize (default = 10)
        String returnType // json xml (default = json)
        ) {
    public static CenterRequestDto from(int pageNum, int pageSize) {
        return new CenterRequestDto(
                pageNum > 0 ? pageNum : 1, // page 기본값 1
                pageSize > 0 ? pageSize : 10, // perPage 기본값 10
                "json" // returnType 기본값 json
                );
    }
}
