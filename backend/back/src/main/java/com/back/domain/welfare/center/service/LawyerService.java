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

        String area1 = normalizeArea1(addressList[0]); // 시/도
        String area2 = addressList[1]; // 군/구

        return searchByDistrict(area1, area2);
    }

    // 서울특별시 -> 서울, 경상남도 -> 경남과 같이 area1 값을 처리
    private String normalizeArea1(String area1) {
        if (area1 == null || area1.length() <= 2) {
            return area1;
        }

        // 앞 두 글자 추출 (서울, 경기, 제주, 전라, 경상, 충청..)
        String prefix = area1.substring(0, 2);

        // 충청남도 -> 충북처럼 남/북 구분이 필요한 지역 처리
        return switch (prefix) {
            case "전라" -> area1.contains("남") ? "전남" : "전북";
            case "경상" -> area1.contains("남") ? "경남" : "경북";
            case "충청" -> area1.contains("남") ? "충남" : "충북";
            default -> prefix;
        };
    }

    @Transactional(readOnly = true)
    public List<LawyerRes> searchByDistrict(String area1, String area2) {

        return lawyerRepository.findByDistrictArea1AndDistrictArea2Containing(area1, area2).stream()
                .map(LawyerRes::new)
                .collect(Collectors.toList());
    }
}
