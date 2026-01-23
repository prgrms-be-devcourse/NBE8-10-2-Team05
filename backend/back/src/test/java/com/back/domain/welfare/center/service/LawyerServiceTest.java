package com.back.domain.welfare.center.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.geo.entity.AddressDto;
import com.back.domain.welfare.center.lawyer.dto.LawyerRes;
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;
import com.back.domain.welfare.center.lawyer.service.LawyerService;

@SpringBootTest
@Transactional
public class LawyerServiceTest {
    @Autowired
    private LawyerCrawlerService lawyerCrawlerService;

    @Autowired
    private LawyerService lawyerService;

    @Test
    @DisplayName("제주도의 서귀포시가 관할 구역인 노무사 검색 및 특정 인물 확인")
    void t1() throws Exception {
        AddressDto addressDto =
                AddressDto.builder().roadAddress("제주특별자치도 서귀포시 부평대로 168").build();

        lawyerCrawlerService.crawlMultiPages("제주", 1, 1);
        List<LawyerRes> result = lawyerService.getFilteredLawyers(addressDto);

        assertThat(result).isNotEmpty();
        assertThat(result)
                .allMatch(lawyerRes -> lawyerRes.districtArea1().equals("제주")
                        && lawyerRes.districtArea2().contains("서귀포"));
        assertThat(result.stream().anyMatch(lawyer -> lawyer.name().equals("강주리")))
                .isTrue();

        System.out.println("조회된 서귀포 노무사 수: " + result.size());
    }

    @Test
    @DisplayName("충청남도의 노무사 검색 및 특정 인물 확인")
    void t2() throws Exception {
        AddressDto addressDto =
                AddressDto.builder().roadAddress("충청남도 공주시 서북구 번영로 156").build();

        lawyerCrawlerService.crawlMultiPages("충남", 1, 1);
        List<LawyerRes> result = lawyerService.getFilteredLawyers(addressDto);

        assertThat(result).isNotEmpty();

        // 지역 필터링 검증 (충남/천안)
        assertThat(result)
                .allMatch(lawyerRes -> lawyerRes.districtArea1().equals("충남")
                        && lawyerRes.districtArea2().contains("공주"));

        assertThat(result.stream().anyMatch(lawyer -> lawyer.name().equals("우재식")))
                .isTrue();

        System.out.println("조회된 충남 노무사 수: " + result.size());
    }
}
