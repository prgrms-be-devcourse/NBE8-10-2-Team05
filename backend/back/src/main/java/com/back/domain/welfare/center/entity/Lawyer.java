package com.back.domain.welfare.center.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "lawyer",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_lawyer_name_corp",
                    columnNames = {"name", "corporation"})
        }) // name + corporation 로 같은 노무사인지 판단 -> 중복 방지
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lawyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String corporation;

    private String districtArea1;
    // 시/도
    private String districtArea2;
    // 군/구
}
