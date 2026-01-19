package com.back.domain.welfare.policy.dto;

import lombok.Builder;

@Builder
public class PolicyResponseDto {
    private Long id;
    private String plcyNo;
    private String plcyNm;
    private String plcyExplnCn;
    private String plcySprtCn;
    private String plcyKywdNm;
    private String sprtTrgtMinAge;
    private String sprtTrgtMaxAge;
    private String zipCd;
    private String schoolCd;
    private String jobCd;
    private String earnMinAmt;
    private String earnMaxAmt;
    private String aplyYmd;
    private String aplyUrlAddr;
    private String plcyAplyMthdCn;
    private String sbmsnDcmntCn;
    private String operInstCdNm;
}
