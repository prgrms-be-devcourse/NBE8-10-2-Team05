package com.back.global.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationLevel implements CodeEnum {
    BELOW_HIGH_SCHOOL("049001", "고졸 미만"),
    HIGH_SCHOOL("049004", "고교 졸업"),
    UNIVERSITY("049007", "대학 졸업"),
    MASTER_DOCTOR("049008", "석·박사");

    private final String code;
    private final String description;
}
