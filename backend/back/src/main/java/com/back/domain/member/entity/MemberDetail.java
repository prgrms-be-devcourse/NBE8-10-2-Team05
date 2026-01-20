package com.back.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_detail")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MemberDetail {

    @Id
    private Long id;

    @Column(length = 5)
    private String regionCode;

    @Enumerated(EnumType.STRING)
    private MarriageStatus marriageStatus;

    private Integer income;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    private String specialStatus;

    @OneToOne
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    public void update(
            String regionCode,
            MarriageStatus marriageStatus,
            Integer income,
            EmploymentStatus employmentStatus,
            EducationLevel educationLevel,
            String specialStatus) {
        this.regionCode = regionCode; // 법정동/시군구 코드
        this.marriageStatus = marriageStatus; // 결혼 여부
        this.income = income; // 소득
        this.employmentStatus = employmentStatus; // 취업 상태
        this.educationLevel = educationLevel; // 학력 요건
        this.specialStatus = specialStatus; // 특화 요건(특이사항)
    }

    public enum MarriageStatus {
        SINGLE,
        MARRIED
    }

    public enum EmploymentStatus {
        EMPLOYED,
        UNEMPLOYED
    }

    public enum EducationLevel {
        HIGH_SCHOOL,
        COLLEGE,
        UNIVERSITY,
        GRADUATE
    }
}
