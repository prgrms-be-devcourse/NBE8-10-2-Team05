package com.back.domain.welfare.center.lawyer.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.lawyer.dto.LawyerReq;
import com.back.domain.welfare.center.lawyer.dto.LawyerRes;
import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerService {
    private final LawyerRepository lawyerRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "lawyer", key = "{#lawyerReq, #pageable}")
    public Page<LawyerRes> searchByDistrict(LawyerReq lawyerReq, Pageable pageable) {

        String area1 = normalizeArea1(lawyerReq.area1());
        // 서울특별시 -> 서울, 전라북도 -> 전북 으로 정규화 위함
        String area2 = lawyerReq.area2();

        Page<Lawyer> lawyerPage;
        if (area2 != null && !area2.isBlank()) {
            lawyerPage = lawyerRepository.findByDistrictArea1AndDistrictArea2Containing(area1, area2, pageable);
        } else {
            lawyerPage = lawyerRepository.findByDistrictArea1AndDistrictArea2Containing(area1, "", pageable);
        }

        return lawyerPage.map(LawyerRes::new);
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
}
