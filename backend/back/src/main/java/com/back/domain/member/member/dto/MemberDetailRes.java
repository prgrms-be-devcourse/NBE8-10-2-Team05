package com.back.domain.member.member.dto;

import java.time.LocalDateTime;

import com.back.domain.member.member.entity.Member.LoginType;
import com.back.domain.member.member.entity.Member.Role;
import com.back.domain.member.member.entity.MemberDetail;
import com.back.global.enumtype.EducationLevel;
import com.back.global.enumtype.EmploymentStatus;
import com.back.global.enumtype.MarriageStatus;

public record MemberDetailRes(
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String name,
        String email,
        String rrnFront,
        String rrnBackFirst,
        LoginType type,
        Role role,
        String regionCode,
        MarriageStatus marriageStatus,
        Integer income,
        EmploymentStatus employmentStatus,
        EducationLevel educationLevel,
        String specialStatus,
        String postcode,
        String roadAddress,
        String hCode,
        Double latitude,
        Double longitude) {
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
                memberDetail.getEducationLevel(),
                memberDetail.getSpecialStatus(),
                memberDetail.getAddress() != null ? memberDetail.getAddress().getPostcode() : null,
                memberDetail.getAddress() != null ? memberDetail.getAddress().getRoadAddress() : null,
                memberDetail.getAddress() != null ? memberDetail.getAddress().getHCode() : null,
                memberDetail.getAddress() != null ? memberDetail.getAddress().getLatitude() : null,
                memberDetail.getAddress() != null ? memberDetail.getAddress().getLongitude() : null);
    }
}
