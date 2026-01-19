package com.back.domain.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_detail")
@Getter
@NoArgsConstructor
public class MemberDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JoinColumn(name = "member_id")
    private Member member;

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
