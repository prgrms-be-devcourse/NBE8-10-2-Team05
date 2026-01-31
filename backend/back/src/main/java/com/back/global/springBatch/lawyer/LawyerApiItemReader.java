package com.back.global.springBatch.lawyer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.back.domain.welfare.center.lawyer.entity.Lawyer;
import com.back.domain.welfare.center.lawyer.service.LawyerCrawlerService;

import lombok.extern.slf4j.Slf4j;

@Component // Spring이 관리하도록 등록
@StepScope
@Slf4j
public class LawyerApiItemReader extends AbstractPagingItemReader<Lawyer> {

    private final LawyerCrawlerService lawyerCrawlerService;
    private final int totalCount;
    private final String[] regionList = {
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    };
    private Integer currentRegionIdx = 0;

    @Value("#{stepExecutionContext['region']}")
    String region;

    // 생성자를 통해 totalCount를 주입받음
    public LawyerApiItemReader(LawyerCrawlerService apiService) {
        this.lawyerCrawlerService = apiService;
        this.totalCount = 6202;
        setPageSize(8); // 한번에 읽을 양 설정
    }

    @Override
    protected void doReadPage() {
        // 결과 저장소 초기화 : Thread-safe한 리스트
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();

        // 읽어야 할 데이터가 totalCount를 넘으면 종료
        if (getPage() * getPageSize() >= totalCount) return;

        boolean success = false;
        while (!success && currentRegionIdx < regionList.length) {
            try {
                // 현재 선택된 키로 API 호출 (페이지 번호 전달)

                //                EstateFetchRequestDto requestDto = EstateFetchRequestDto.builder()
                //                    .pageNo(1)
                //                    .numOfRows(getPageSize())
                //                    .build();
                //                EstateFetchResponseDto responseDto = estateApiClient.fetchEstatePage(requestDto);

                List<Lawyer> data = lawyerCrawlerService.crawlEachPage(regionList[currentRegionIdx], getPage());
                results.addAll(data);
                success = true;

                currentRegionIdx++;

            } catch (HttpClientErrorException e) { // 4xx 에러
                log.error("클라이언트 에러 발생: {}", e.getMessage());
                throw e; // 그 외 400, 404 등은 그냥 에러 발생
            } catch (HttpServerErrorException e) { // 5xx 에러
                log.error("API 서버 내부 에러: {}", e.getMessage());
                throw e; // 서버 에러는 키 교체로 해결되지 않으므로 throw
            } catch (Exception e) {
                log.error("알 수 없는 에러: {}", e.getMessage());
                throw e;
            }
        }
    }
}
