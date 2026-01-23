package com.back.domain.welfare.center.dto;

import com.back.domain.welfare.center.entity.Lawyer;

public record LawyerRes(Long id, String name, String corporation, String districtArea1, String districtArea2) {
    public LawyerRes(Lawyer lawyer) {
        this(
                lawyer.getId(),
                lawyer.getName(),
                lawyer.getCorporation(),
                lawyer.getDistrictArea1(),
                lawyer.getDistrictArea2());
    }
}
