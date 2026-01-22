package com.back.domain.welfare.center.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.dto.LawyerRes;
import com.back.domain.welfare.center.repository.LawyerRepository;
import com.back.global.geo.entity.AddressDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerService {
    private final LawyerRepository lawyerRepository;

    @Transactional(readOnly = true)
    public List<LawyerRes> getFilteredLawyers(AddressDto addressDto) {
        String address = addressDto.roadAddress(); // 도로명주소 받아옴

        String[] addressList = address.split(" ");

        String province = addressList[0]; // 시/도
        String gunGu = addressList[1]; // 군/구

        return searchByDistrict(province, gunGu);
    }

    @Transactional(readOnly = true)
    public List<LawyerRes> searchByDistrict(String area1, String area2) {

        return lawyerRepository.findByDistrictArea1AndDistrictArea2Containing(area1, area2).stream()
                .map(LawyerRes::new)
                .collect(Collectors.toList());
    }
}
