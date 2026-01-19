package com.back.domain.welfare.policy.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PolicyRequestDto {
    private Integer sprtTrgtMinAge;
    private Integer sprtTrgtMaxAge;
    private String zipCd;
    private String schoolCd;
    private String jobCd;
    private Integer earnMinAmt;
    private Integer earnMaxAmt;
}
