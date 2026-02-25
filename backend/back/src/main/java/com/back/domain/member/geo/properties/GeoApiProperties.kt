package com.back.domain.member.geo.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom.api.geo")
@JvmRecord
data class GeoApiProperties(val key: String?, val url: String?)
