package com.back.domain.member.member.dto;

import java.time.LocalDateTime;

import com.back.domain.member.member.entity.Member.LoginType;
import com.back.domain.member.member.entity.Member.Role;
import com.back.domain.member.member.entity.MemberDetail;
import com.back.domain.member.member.entity.MemberDetail.EducationLevel;
import com.back.domain.member.member.entity.MemberDetail.EmploymentStatus;
import com.back.domain.member.member.entity.MemberDetail.MarriageStatus;

public record MemberDetailRes(
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String name,
        String email,
        Integer rrnFront,
        Integer rrnBackFirst,
        LoginType type,
        Role role,
        String regionCode,
        MarriageStatus marriageStatus,
        Integer income,
        EmploymentStatus employmentStatus,
        EducationLevel educationLevel) {
    public MemberDetailRes(MemberDetail memberDetail) {
        this(
                memberDetail.getMember().getCreatedAt(),
                memberDetail.getMember().getModifiedAt(),
                memberDetail.getMember().getName(),
                memberDetail.getMember().getEmail(),
                memberDetail.getMember().getRrnFront(),
                memberDetail.getMember().getRrnBackFirst(),
                memberDetail.getMember().getType(),
                memberDetail.getMember().getRole(),
                memberDetail.getRegionCode(),
                memberDetail.getMarriageStatus(),
                memberDetail.getIncome(),
                memberDetail.getEmploymentStatus(),
                memberDetail.getEducationLevel());
    }
}
