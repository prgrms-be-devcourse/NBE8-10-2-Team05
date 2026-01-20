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
    private Integer id;

    private String plcyNo; // 정책번호
    private String plcyNm; // 정책명
    private String plcyExplnCn; // 정책설명내용
    private String plcySprtCn; // 정책지원내용
    private String plcyKywdNm; // 정책키워드명

    private String sprtTrgtMinAge; // 지원대상최소연령
    private String sprtTrgtMaxAge; // 지원대상최대연령

    private String zipCd; // 정책거주지역코드
    private String schoolCd; // 정책학력요건코드
    private String jobCd; // 정책취업요건코드

    private String earnMinAmt; // 소득최소금액
    private String earnMaxAmt; // 소득최대금액

    private String aplyYmd; // 신청기간
    private String aplyUrlAddr; // 신청URL주소
    private String aplyMthdCn; // 정책신청방법내용
    private String sbmsnDcmntCn; // 제출서류내용
    private String operInstCdNm; // 운영기관코드명
}
