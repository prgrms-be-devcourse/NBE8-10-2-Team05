package com.back.domain.welfare.policy.dto;

import java.util.List;

/**
 * 청년정책 Open API 응답 DTO
 * - API 구조 그대로 표현
 */
public record PolicyFetchResponseDto(int resultCode, String resultMessage, Result result) {

    /**
     * result 영역
     */
    public record Result(Pagging pagging, List<PolicyItem> youthPolicyList) {}

    /**
     * 페이징 정보
     */
    public record Pagging(int totCount, int pageNum, int pageSize) {}

    /**
     * 청년 정책 단건
     */
    public record PolicyItem(
            String plcyNo,
            String bscPlanCycl,
            String bscPlanPlcyWayNo,
            String bscPlanFcsAsmtNo,
            String bscPlanAsmtNo,
            String pvsnInstGroupCd,
            String plcyPvsnMthdCd,
            String plcyAprvSttsCd,
            String plcyNm,
            String plcyKywdNm,
            String plcyExplnCn,
            String lclsfNm,
            String mclsfNm,
            String plcySprtCn,
            String sprvsnInstCd,
            String sprvsnInstCdNm,
            String sprvsnInstPicNm,
            String operInstCd,
            String operInstCdNm,
            String operInstPicNm,
            String sprtSclLmtYn,
            String aplyPrdSeCd,
            String bizPrdSeCd,
            String bizPrdBgngYmd,
            String bizPrdEndYmd,
            String bizPrdEtcCn,
            String plcyAplyMthdCn,
            String srngMthdCn,
            String aplyUrlAddr,
            String sbmsnDcmntCn,
            String etcMttrCn,
            String refUrlAddr1,
            String refUrlAddr2,
            String sprtSclCnt,
            String sprtArvlSeqYn,
            String sprtTrgtMinAge,
            String sprtTrgtMaxAge,
            String sprtTrgtAgeLmtYn,
            String mrgSttsCd,
            String earnCndSeCd,
            String earnMinAmt,
            String earnMaxAmt,
            String earnEtcCn,
            String addAplyQlfcCndCn,
            String ptcpPrpTrgtCn,
            String inqCnt,
            String rgtrInstCd,
            String rgtrInstCdNm,
            String rgtrUpInstCd,
            String rgtrUpInstCdNm,
            String rgtrHghrkInstCd,
            String rgtrHghrkInstCdNm,
            String zipCd,
            String plcyMajorCd,
            String jobCd,
            String schoolCd,
            String aplyYmd,
            String frstRegDt,
            String lastMdfcnDt,
            String sbizCd) {}
}
