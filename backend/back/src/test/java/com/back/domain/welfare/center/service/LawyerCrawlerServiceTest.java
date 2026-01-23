package com.back.domain.welfare.center.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.center.lawyer.repository.LawyerRepository;
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;
import com.back.domain.welfare.center.lawyer.service.LawyerService;

@SpringBootTest
@Transactional
class LawyerCrawlerTest {
    @Autowired
    private LawyerCrawlerService lawyerCrawlerService;

    @Autowired
    private LawyerService lawyerService;

    @Autowired
    private LawyerRepository lawyerRepository;

    @Test
    @DisplayName("서울 1,2페이지 크롤링 테스트")
    void t1() throws Exception {
        long count = lawyerRepository.count();

        lawyerCrawlerService.crawlMultiPages("서울", 1, 2);

        List<Lawyer> savedLawyers = lawyerRepository.findAll();

        assertThat(savedLawyers.size()).isGreaterThanOrEqualTo((int) count);
        assertThat(savedLawyers).isNotEmpty();
        Lawyer firstLawyer = savedLawyers.get(0);
        assertThat(firstLawyer.getName()).isNotBlank();
    }

    @Test
    @DisplayName("서울 첫 페이지 노무사 정보 확인")
    void t2() throws Exception {
        lawyerCrawlerService.crawlMultiPages("서울", 1, 1);

        List<Lawyer> result = lawyerRepository.findAll();
        assertThat(result).isNotEmpty();

        Lawyer targetLawyer = result.stream()
                .filter(l -> l.getName().equals("조갑식"))
                .findFirst()
                .get();

        assertThat(targetLawyer.getDistrictArea1()).isEqualTo("서울");
        assertThat(targetLawyer.getDistrictArea2()).containsAnyOf("중구", "종로구", "서초구", "동대문구");
        assertThat(targetLawyer.getCorporation()).isEqualTo("노무법인 케이에스");
    }

    @Test
    @DisplayName("서울 지역의 마지막 페이지 번호 조회 확인")
    void t3() throws Exception {
        int lastPage = lawyerCrawlerService.getLastPage("서울");

        assertThat(lastPage).isGreaterThan(0);
        assertThat(lastPage).isGreaterThanOrEqualTo(311);

        System.out.println("서울 마지막 페이지: " + lastPage);
    }
}
