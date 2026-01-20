package com.back.domain.welfare.center.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.welfare.center.entity.Lawyer;
import com.back.domain.welfare.center.repository.LawyerRepository;

@SpringBootTest
@Transactional
class LawyerCrawlerTest {
    @Autowired
    private LawyerCrawlerService lawyerCrawlerService;

    @Autowired
    private LawyerRepository lawyerRepository;

    @Test
    @DisplayName("1,2페이지 크롤링 테스트")
    void t1() throws Exception {
        long count = lawyerRepository.count();

        lawyerCrawlerService.crawlMultiPages(1, 2);

        List<Lawyer> savedLawyers = lawyerRepository.findAll();
        assertThat(savedLawyers.size()).isGreaterThan((int) count);

        Lawyer firstLawyer = savedLawyers.get(0);
        assertThat(firstLawyer.getName()).isNotBlank();
        assertThat(firstLawyer.getDistrict()).isNotBlank();
        assertThat(firstLawyer.getCorporation()).isNotBlank();
    }

    @Test
    @DisplayName("첫 페이지 노무사 정보 확인")
    void t2() throws Exception {
        lawyerCrawlerService.crawlMultiPages(1, 1);

        List<Lawyer> result = lawyerRepository.findAll();
        assertThat(result).isNotEmpty();

        Lawyer targetLawyer = result.stream()
                .filter(l -> l.getName().equals("조갑식"))
                .findFirst()
                .get();

        assertThat(targetLawyer.getDistrict()).containsAnyOf("중구", "종로구", "서초구", "동대문구");
        assertThat(targetLawyer.getCorporation()).isEqualTo("노무법인 케이에스");
    }
}
