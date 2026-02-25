package com.back.domain.welfare.estate.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EstateDto(
    @JsonProperty("suplyHoCo") val suplyHoCo: String? = null,
    @JsonProperty("pblancId") val pblancId: String? = null,
    @JsonProperty("houseSn") val houseSn: Int? = null,
    @JsonProperty("sttusNm") val sttusNm: String? = null,
    @JsonProperty("pblancNm") val pblancNm: String? = null, // [시흥정왕 1블록 행복주택] 예비 입주자모집
    @JsonProperty("suplyInsttNm") val suplyInsttNm: String? = null,
    @JsonProperty("houseTyNm") val houseTyNm: String? = null, // "아파트"
    @JsonProperty("suplyTyNm") val suplyTyNm: String? = null, // "행복주택", "전세임대"
    @JsonProperty("rcritPblancDe") val rcritPblancDe: String? = null,
    @JsonProperty("url") val url: String? = null,
    @JsonProperty("hsmpNm") val hsmpNm: String? = null,
    @JsonProperty("brtcNm") val brtcNm: String? = null, // "경기도",
    @JsonProperty("signguNm") val signguNm: String? = null, // "시흥시"
    @JsonProperty("fullAdres") val fullAdres: String? = null, // "경기도 시흥시 정왕동 1799-2 ",
    @JsonProperty("rentGtn") val rentGtn: Long? = null,
    @JsonProperty("mtRntchrg") val mtRntchrg: Long? = null,
    @JsonProperty("beginDe") val beginDe: String? = null, // "20260116"
    @JsonProperty("endDe") val endDe: String? = null, // "20260116"
    @JsonProperty("pnu") val pnu: String? = null // 시군구 코드 추출을 위해 추가
) {
    // Java에서 기존처럼 .builder()를 호출해야 한다면 아래 companion object를 유지합니다.
    companion object {
        @JvmStatic
        fun builder() = EstateDtoBuilder()
    }

    // Java 호환성을 위한 빌더 (Kotlin만 쓴다면 삭제해도 무방합니다)
    class EstateDtoBuilder {
        private var suplyHoCo: String? = null
        private var pblancId: String? = null
        private var houseSn: Int? = null
        private var sttusNm: String? = null
        private var pblancNm: String? = null
        private var suplyInsttNm: String? = null
        private var houseTyNm: String? = null
        private var suplyTyNm: String? = null
        private var rcritPblancDe: String? = null
        private var url: String? = null
        private var hsmpNm: String? = null
        private var brtcNm: String? = null
        private var signguNm: String? = null
        private var fullAdres: String? = null
        private var rentGtn: Long? = null
        private var mtRntchrg: Long? = null
        private var beginDe: String? = null
        private var endDe: String? = null
        private var pnu: String? = null

        fun suplyHoCo(v: String) = apply { this.suplyHoCo = v }
        fun pblancId(v: String?) = apply { this.pblancId = v }
        fun houseSn(v: Int?) = apply { this.houseSn = v }
        fun sttusNm(v: String?) = apply { this.sttusNm = v }
        fun pblancNm(v: String?) = apply { this.pblancNm = v }
        fun suplyInsttNm(v: String?) = apply { this.suplyInsttNm = v }
        fun houseTyNm(v: String?) = apply { this.houseTyNm = v }
        fun suplyTyNm(v: String?) = apply { this.suplyTyNm = v }
        fun rcritPblancDe(v: String?) = apply { this.rcritPblancDe = v }
        fun url(v: String?) = apply { this.url = v }
        fun hsmpNm(v: String?) = apply { this.hsmpNm = v }
        fun brtcNm(v: String?) = apply { this.brtcNm = v }
        fun signguNm(v: String?) = apply { this.signguNm = v }
        fun fullAdres(v: String?) = apply { this.fullAdres = v }
        fun rentGtn(v: Long?) = apply { this.rentGtn = v }
        fun mtRntchrg(v: Long?) = apply { this.mtRntchrg = v }
        fun beginDe(v: String?) = apply { this.beginDe = v }
        fun endDe(v: String?) = apply { this.endDe = v }
        fun pnu(v: String?) = apply { this.pnu = v }

        fun build() = EstateDto(
            suplyHoCo, pblancId, houseSn, sttusNm, pblancNm, suplyInsttNm, houseTyNm,
            suplyTyNm, rcritPblancDe, url, hsmpNm, brtcNm, signguNm, fullAdres,
            rentGtn, mtRntchrg, beginDe, endDe, pnu
        )
    }
}
