package com.back.domain.member.member.entity;

import com.back.domain.member.geo.entity.Address;
import com.back.global.enumtype.EducationLevel;
import com.back.global.enumtype.EmploymentStatus;
import com.back.global.enumtype.MarriageStatus;
import com.back.global.enumtype.SpecialStatus;

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

    private SpecialStatus specialStatus;

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
            SpecialStatus specialStatus) {
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
        // 카카오 우편번호 검색 API 제공
        String postcode; // 우편번호
        String addressName; // 전체 주소
        String sigunguCode; // 41135 시/군/구 코드
        String bCode;
        // 4113511000	법정동/법정리 코드
        String roadAddress;
        // 도로명주소
        String sigungu;
        // 시/군/구 이름 "성남시 분당구"
        String sido;
        // 도/시 이름 "경기"

        // 카카오 Local API 제공
        // 도로명 주소로 가져온다.
        String hCode;
        // "4514069000" 행정동 코드
        Double latitude;
        // 위도
        Double longitude;
        // 경도

        public static Address from(com.back.domain.member.geo.entity.Address dto) {
            return Address.builder()
                    .postcode(dto.postcode()) // 우편번호
                    .addressName(dto.addressName()) // 전체 주소
                    .sigunguCode(dto.sigunguCode()) // 41135 시/군/구 코드
                    .bCode(dto.bCode()) // 법정동/법정리 코드
                    .roadAddress(dto.roadAddress()) // 도로명주소
                    .sigungu(dto.sigungu()) // 시/군/구 이름 "성남시 분당구"
                    .sido(dto.sido()) // 도/시 이름 "경기"
                    .hCode(dto.hCode()) // 행정동 코드
                    .latitude(dto.latitude()) // 위도
                    .longitude(dto.longitude()) // 경도
                    .build();
        }
    }

    public void updateAddress(com.back.domain.member.geo.entity.Address address) {
        this.address = Address.from(address);
    }
}
