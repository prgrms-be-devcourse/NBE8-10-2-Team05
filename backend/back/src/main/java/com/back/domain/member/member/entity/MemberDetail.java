package com.back.domain.member.member.entity;

import com.back.domain.member.geo.entity.Address;
import com.back.global.enumtype.EducationLevel;
import com.back.global.enumtype.EmploymentStatus;
import com.back.global.enumtype.MarriageStatus;

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

    @Embedded
    private Address address;

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

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        private String postcode; // 우편번호
        private String roadAddress; // 도로명 주소
        private String hCode; // 행정동 코드
        private Double latitude; // 위도
        private Double longitude; // 경도

        public static Address from(com.back.domain.member.geo.entity.Address dto) {
            return Address.builder()
                    .postcode(dto.postcode())
                    .roadAddress(dto.roadAddress())
                    .hCode(dto.hCode())
                    .latitude(dto.latitude())
                    .longitude(dto.longitude())
                    .build();
        }
    }

    public void updateAddress(com.back.domain.member.geo.entity.Address address) {
        this.address = Address.from(address);
    }
}
