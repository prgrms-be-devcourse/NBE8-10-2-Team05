package com.back.global.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentStatus implements CodeEnum {
    EMPLOYED("013001", "재직자"),
    SELF_EMPLOYED("013002", "자영업자"),
    UNEMPLOYED("013003", "미취업자"),
    FREELANCER("013004", "프리랜서");

    private final String code;
    private final String description;
}
