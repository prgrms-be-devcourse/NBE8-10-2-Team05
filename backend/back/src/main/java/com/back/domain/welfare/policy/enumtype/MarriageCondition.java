package com.back.domain.welfare.policy.enumtype;

import com.back.global.enumtype.CodeEnum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 결혼상태코드
@Getter
@RequiredArgsConstructor
public enum MarriageCondition implements CodeEnum {
    MARRIED("055001", "기혼"),
    SINGLE("055002", "미혼"),
    NO_LIMIT("ALL", "제한없음");

    private final String code;
    private final String description;
}
