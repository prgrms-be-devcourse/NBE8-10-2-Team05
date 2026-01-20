package com.back.domain.welfare.estate.dto;

import lombok.Builder;

@Builder
public record EstateFetchRequestDto(
        String serviceKey, // 공공데이터포털에서 받은 인증키
        String brtcCode, // 광역시도 코드
        String signguCode, // 시군구 코드
        Integer numOfRows, // 조회될 목록의 페이지당 데이터 개수 (기본값:10)
        Integer pageNo, // 조회될 페이지의 번호 (기본값:1)
        String suplyTy, // 공급유형
        String houseTy, // 주택유형
        String lfstsTyAt, // 전세형 모집 여부 (Y/N 등)
        String bassMtRntchrgSe, // 월임대료 구분
        String yearMtBegin, // 모집공고월시작(YYYYMM)
        String yearMtEnd // 모집공고월시작(YYYYMM)
        ) {}
