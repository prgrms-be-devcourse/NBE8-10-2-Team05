package com.back.domain.welfare.policy.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PolicySearchResponseDto(
        Integer id,
        String plcyNo,
        String plcyNm,
        String plcyExplnCn,
        String plcySprtCn,
        String plcyKywdNm,
        String sprtTrgtMinAge,
        String sprtTrgtMaxAge,
        String zipCd,
        String schoolCd,
        String jobCd,
        String earnMinAmt,
        String earnMaxAmt,
        String aplyYmd,
        String aplyUrlAddr,
        String aplyMthdCn,
        String sbmsnDcmntCn,
        String operInstCdNm) {
    @QueryProjection
    public PolicySearchResponseDto {}
}
