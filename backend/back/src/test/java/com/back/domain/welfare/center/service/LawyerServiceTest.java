package com.back.domain.welfare.center.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.lawyer.dto.LawyerReq;
import com.back.domain.welfare.center.lawyer.dto.LawyerRes;
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;
import com.back.domain.welfare.center.lawyer.service.LawyerService;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class LawyerServiceTest {
    @Autowired
    private LawyerCrawlerService lawyerCrawlerService;

    @Autowired
    private LawyerService lawyerService;

    @Test
    @DisplayName("제주도의 서귀포시가 관할 구역인 노무사 검색 - 페이징 및 특정 인물 확인")
    void t1() throws Exception {

        lawyerCrawlerService.crawlMultiPages("제주", 1, 1);

        LawyerReq req = new LawyerReq("제주특별자치도", "서귀포");
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 30);

        Page<LawyerRes> resultPage = lawyerService.searchByDistrict(req, pageable);
        java.util.List<LawyerRes> content = resultPage.getContent();

        assertThat(content).isNotEmpty();
        assertThat(content)
                .allMatch(lawyerRes -> lawyerRes.districtArea1().equals("제주")
                        && lawyerRes.districtArea2().contains("서귀포"));
        assertThat(content.stream().anyMatch(lawyer -> lawyer.name().equals("강주리")))
                .isTrue();
    }

    @Test
    @DisplayName("충청남도의 공주시 노무사 검색 - 페이징 및 특정 인물 확인")
    void t2() throws Exception {
        lawyerCrawlerService.crawlMultiPages("충남", 1, 1);

        LawyerReq req = new LawyerReq("충청남도", "공주");
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 30);

        org.springframework.data.domain.Page<LawyerRes> resultPage = lawyerService.searchByDistrict(req, pageable);
        java.util.List<LawyerRes> content = resultPage.getContent();

        assertThat(content).isNotEmpty();
        assertThat(content)
                .allMatch(lawyerRes -> lawyerRes.districtArea1().equals("충남")
                        && lawyerRes.districtArea2().contains("공주"));
        assertThat(content.stream().anyMatch(lawyer -> lawyer.name().equals("우재식")))
                .isTrue();

        System.out.println("검색된 공주 노무사 총 수: " + resultPage.getTotalElements());
    }
}
