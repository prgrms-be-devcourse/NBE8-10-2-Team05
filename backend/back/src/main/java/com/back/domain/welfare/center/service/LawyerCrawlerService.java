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
    private final String BASE_URL = "https://www.youthlabor.co.kr/company/search?area=&area2=&text=&page=";
    private static final int BATCH_SIZE = 100;
    private final LawyerSaveService lawyerSaveService;

    public void crawlMultiPages(int startPage, int endPage) {
        List<Lawyer> lawyerList = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            String url = BASE_URL + i;
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
                            .district(region)
                            .name(name)
                            .corporation(company)
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
}
