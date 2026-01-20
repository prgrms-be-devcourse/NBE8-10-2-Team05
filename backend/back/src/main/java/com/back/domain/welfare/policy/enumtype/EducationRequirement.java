package com.back.domain.welfare.policy.enumtype;

import com.back.global.enumtype.CodeEnum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 정책 학력요건코드
@Getter
@RequiredArgsConstructor
public enum EducationRequirement implements CodeEnum {
    BELOW_HIGH_SCHOOL("049001", "고졸 미만"),
    HIGH_SCHOOL_ENROLLED("049002", "고교 재학"),
    HIGH_SCHOOL_EXPECTED("049003", "고졸 예정"),
    HIGH_SCHOOL_GRADUATED("049004", "고교 졸업"),
    UNIVERSITY_ENROLLED("049005", "대학 재학"),
    UNIVERSITY_EXPECTED("049006", "대졸 예정"),
    UNIVERSITY_GRADUATED("049007", "대학 졸업"),
    MASTER_DOCTOR("049008", "석·박사"),
    NO_LIMIT("ALL", "제한없음");

    private final String code;
    private final String description;
}
