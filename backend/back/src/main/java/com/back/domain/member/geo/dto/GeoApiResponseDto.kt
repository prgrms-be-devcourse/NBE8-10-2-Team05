package com.back.domain.member.geo.dto

data class GeoApiResponseDto(val meta: Meta?, @JvmField val documents: MutableList<Document?>?) {
    data class Meta(
        val totalCount: Int,
        val pageableCount: Int,
        val isEnd: Boolean
    )

    data class Document(
        val addressName: String?,
        val x: String?,  // 경도
        val y: String?,  // 위도
        val addressType: String?,
        @JvmField val address: Address?,
        val roadAddress: RoadAddress?
    )

    data class Address(
        val addressName: String?,
        val region1depthName: String?,
        val region2depthName: String?,
        val region3depthName: String?,
        @JvmField val hCode: String?,  // 행정동 코드
        val bCode: String?,  // 법정동 코드
        @JvmField val x: String?,
        @JvmField val y: String?
    )

    data class RoadAddress(
        val addressName: String?,
        val zoneNo: String?,  // 우편번호
        val x: String?,
        val y: String?
    )
}
