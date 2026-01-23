package com.back.domain.welfare.center.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.back.domain.welfare.center.entity.Lawyer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerCrawlerService {
    private final String SEARCH_URL = "https://www.youthlabor.co.kr/company/search?text=&area=%s&area2=&page=%d";
    // 시/도인 area1, 페이지 숫자인 page를 넣어서 찾을 수 있도록
    private static final int BATCH_SIZE = 100;
    private final LawyerSaveService lawyerSaveService;
    private final String[] regionList = {
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    };

    public void crawlAllPages() {
        // 시/도 순회
        for (String area1 : regionList) {
            int lastPage = getLastPage(area1);
            crawlMultiPages(area1, 1, lastPage);
        }
        // 크롤링 테스트 용이성을 위해서 crawlMultiPages. getLastPage 따로 분리했음.
    }

    public void crawlMultiPages(String area1, int startPage, int endPage) {
        List<Lawyer> lawyerList = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            String url = String.format(SEARCH_URL, area1, i);
            Elements rows = crawlAndSelectRows(url);
            // 테이블 내부 각각 행의 정보을 받아옴

            for (Element row : rows) {
                // 받아온 행들을 순회, td에서 데이터 추출
                Elements cols = row.select("td");

                // 데이터가 3개 다 있는 행인지 확인하고 출력
                if (cols.size() >= 3) {
                    String region = cols.get(0).text();
                    String name = cols.get(1).text();
                    String company = cols.get(2).text();

                    Lawyer lawyer = Lawyer.builder()
                            .name(name)
                            .corporation(company)
                            .districtArea1(area1) // 검색어에서 가져온 시/도
                            .districtArea2(region) // 검색어에서 가져온 군/구
                            .build();

                    lawyerList.add(lawyer);
                }
                // 100건마다 즉시 저장
                if (lawyerList.size() >= BATCH_SIZE) {
                    lawyerSaveService.saveList(lawyerList);
                    lawyerList.clear();
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 마지막 남은 데이터 저장
        if (!lawyerList.isEmpty()) {
            lawyerSaveService.saveList(lawyerList);
            lawyerList.clear();
        }
    }

    public Elements crawlAndSelectRows(String url) {
        try {
            Document doc =
                    Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(10000).get();
            return doc.select("table tbody tr");
            // table의 -> tbody -> tr 조회
        } catch (IOException e) {
            System.err.println(e.getMessage());
            // 에러 발생 시 프로그램이 멈추지 않도록, 빈 Elements 반환
            return new Elements();
        }
    }

    // 특정 지역의 마지막 페이지 숫자 받아오기
    public int getLastPage(String area1) {

        try {
            String url = String.format(SEARCH_URL, area1, 1);
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
            Element lastPage = doc.select("a.last").first();
            // 해당 사이트의 HTML의 링크 태그 찾아서, 마지막 페이지 링크 뽑아옴

            if (lastPage != null) {
                String href = lastPage.attr("href");

                String[] href_list = href.split("=");
                String lastPageNum = href_list[href_list.length - 1];
                // url은 https://...&area2=&text=&page=(페이지 숫자) 의 구조로 이루어져 있으니,
                // '=' 기준으로 스플릿해서 배열의 가장 마지막 요소 뽑아옴 -> 마지막 페이지 숫자 나옴.

                return Integer.parseInt(lastPageNum);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return 1;
    }
}
