package com.back.domain.welfare.center.lawyer.dto;

import jakarta.validation.constraints.NotBlank;

public record LawyerReq(@NotBlank String area1, String area2) {}
