package com.back.domain.welfare.center.entity;

import com.back.domain.welfare.center.center.dto.CenterResponseDto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "center")
@Getter
@NoArgsConstructor
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private String name;
    private String address;
    private String contact;
    private String operator;
    private String corpType;

    public Center(String location, String name, String address, String contact, String operator, String corpType) {
        this.location = location;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.operator = operator;
        this.corpType = corpType;
    }

    public static Center dtoToEntity(CenterResponseDto.CenterDto centerDto) {
        return new Center(
                centerDto.city(), // location
                centerDto.facilityName(), // name
                centerDto.address(), // address
                centerDto.phoneNumber(), // contact
                centerDto.operator(), // operator
                centerDto.corporationType() // corpType
                );
    }
}
