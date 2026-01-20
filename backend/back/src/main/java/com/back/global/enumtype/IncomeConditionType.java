package com.back.global.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IncomeConditionType implements CodeEnum {
    ANNUAL_INCOME("043002", "연소득"),
    ETC("043003", "기타");

    private final String code;
    private final String description;
}
