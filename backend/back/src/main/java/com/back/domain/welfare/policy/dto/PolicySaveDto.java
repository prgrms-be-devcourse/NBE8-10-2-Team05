package com.back.domain.welfare.policy.dto;

public record PolicySaveDto(
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
        String operInstCdNm,
        String rawJson) {}
