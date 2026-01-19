package com.back.domain.welfare.estate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstateItem(
        @JsonProperty("suplyHoCo") String suplyHoCo,
        @JsonProperty("pblancId") String pblancId,
        @JsonProperty("houseSn") Integer houseSn,
        @JsonProperty("sttusNm") String sttusNm,
        @JsonProperty("pblancNm") String pblancNm, // [시흥정왕 1블록 행복주택] 예비 입주자모집
        @JsonProperty("suplyInsttNm") String suplyInsttNm,
        @JsonProperty("houseTyNm") String houseTyNm, // "아파트"
        @JsonProperty("suplyTyNm") String suplyTyNm, // "행복주택", "전세임대"
        @JsonProperty("rcritPblancDe") String rcritPblancDe,
        @JsonProperty("url") String url,
        @JsonProperty("hsmpNm") String hsmpNm,
        @JsonProperty("brtcNm") String brtcNm, // "경기도",
        @JsonProperty("signguNm") String signguNm, // "시흥시"
        @JsonProperty("fullAdres") String fullAdres, // "경기도 시흥시 정왕동 1799-2 ",
        @JsonProperty("rentGtn") Long rentGtn,
        @JsonProperty("mtRntchrg") Long mtRntchrg,
        @JsonProperty("beginDe") String beginDe, // "20260116"
        @JsonProperty("endDe") String endDe // "20260116"
        ) {
    // public static
}
