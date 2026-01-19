package com.back.domain.welfare.estate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstateItem(
        @JsonProperty("suplyHoCo") String suplyHoCo,
        @JsonProperty("pblancId") String pblancId,
        @JsonProperty("houseSn") Integer houseSn,
        @JsonProperty("sttusNm") String sttusNm,
        @JsonProperty("pblancNm") String pblancNm,
        @JsonProperty("suplyInsttNm") String suplyInsttNm,
        @JsonProperty("houseTyNm") String houseTyNm,
        @JsonProperty("suplyTyNm") String suplyTyNm,
        @JsonProperty("rcritPblancDe") String rcritPblancDe,
        @JsonProperty("url") String url,
        @JsonProperty("hsmpNm") String hsmpNm,
        @JsonProperty("brtcNm") String brtcNm,
        @JsonProperty("signguNm") String signguNm,
        @JsonProperty("fullAdres") String fullAdres,
        @JsonProperty("rentGtn") Long rentGtn,
        @JsonProperty("mtRntchrg") Long mtRntchrg,
        @JsonProperty("beginDe") String beginDe,
        @JsonProperty("endDe") String endDe) {}
