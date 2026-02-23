package com.back.global.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MarriageStatus implements CodeEnum {
    MARRIED("055001", "기혼"),
    SINGLE("055002", "미혼");

    private final String code;
    private final String description;
}
