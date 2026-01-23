package com.back.domain.welfare.policy.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PolicySearchRequestDto(
        Integer sprtTrgtMinAge,
        Integer sprtTrgtMaxAge,
        String zipCd,
        String schoolCd,
        String jobCd,
        Integer earnMinAmt,
        Integer earnMaxAmt) {
    @QueryProjection
    public PolicySearchRequestDto {}
}
