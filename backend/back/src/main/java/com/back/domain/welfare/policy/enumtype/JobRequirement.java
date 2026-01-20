package com.back.domain.welfare.policy.enumtype;

import com.back.global.enumtype.CodeEnum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 정책 취업요건 코드
@Getter
@RequiredArgsConstructor
public enum JobRequirement implements CodeEnum {
    EMPLOYED("013001", "재직자"),
    SELF_EMPLOYED("013002", "자영업자"),
    UNEMPLOYED("013003", "미취업자"),
    FREELANCER("013004", "프리랜서"),
    DAILY_WORKER("013005", "일용근로자"),
    PRE_STARTUP("013006", "(예비)창업자"),
    NO_LIMIT("ALL", "제한없음");

    private final String code;
    private final String description;
}
