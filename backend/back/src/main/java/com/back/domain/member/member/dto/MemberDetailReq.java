package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.MemberDetail.EducationLevel;
import com.back.domain.member.member.entity.MemberDetail.EmploymentStatus;
import com.back.domain.member.member.entity.MemberDetail.MarriageStatus;

public record MemberDetailReq(
        String regionCode,
        MarriageStatus marriageStatus,
        Integer income,
        EmploymentStatus employmentStatus,
        EducationLevel educationLevel,
        String specialStatus) {}
