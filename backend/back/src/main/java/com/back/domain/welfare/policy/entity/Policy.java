package com.back.domain.welfare.policy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "policy")
@Getter
@NoArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String aplyMthdCn;
    private String sbmsnDcmntCn;
    private String operInstCdNm;
}
