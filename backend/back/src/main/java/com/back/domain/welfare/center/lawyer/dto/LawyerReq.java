package com.back.domain.welfare.center.lawyer.dto;

import jakarta.validation.constraints.NotBlank;

public record LawyerReq(
        @NotBlank String area1, // 시/도
        String area2 // 군/구
        ) {}
