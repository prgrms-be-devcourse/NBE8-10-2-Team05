package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import com.back.global.enumtype.EducationLevel;
import com.back.global.enumtype.EmploymentStatus;
import com.back.global.enumtype.MarriageStatus;
import com.back.global.enumtype.SpecialStatus;

public record MemberDetailReq(
        // Member 정보
        String name,
        String email,
        String rrnFront,
        String rrnBackFirst,
        Member.LoginType type,
        Member.Role role,

        // MemberDetail 정보
        String regionCode,
        MarriageStatus marriageStatus,
        Integer income,
        EmploymentStatus employmentStatus,
        EducationLevel educationLevel,
        SpecialStatus specialStatus) {}
