package com.back.global.springBatch.center;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;

import com.back.domain.welfare.center.center.dto.CenterApiRequestDto;
import com.back.domain.welfare.center.center.dto.CenterApiResponseDto;
import com.back.domain.welfare.center.center.service.CenterApiService;

@Component // Spring이 관리하도록 등록
@StepScope // 매우 중요! Step 실행 시점에 생성되어야 함
public class CenterApiItemReader extends AbstractPagingItemReader<CenterApiResponseDto.CenterDto> {

    private final CenterApiService centerApiService;
    private final int totalCount;
    private List<String> apiKeys = List.of("1", "2", "3");
    private Integer currentKeyIdx = 0;

    // 생성자를 통해 totalCount를 주입받음
    public CenterApiItemReader(CenterApiService apiService) {
        this.centerApiService = apiService;
        this.totalCount = 1000;
        setPageSize(1000); // 한번에 읽을 양 설정
    }

    @Override
    protected void doReadPage() {
        // 결과 저장소 초기화 : Thread-safe한 리스트
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();

        // 읽어야 할 데이터가 totalCount를 넘으면 종료
        if (getPage() * getPageSize() >= totalCount) return;

        boolean success = false;
        while (!success && currentKeyIdx < apiKeys.size()) {
            try {
                // 현재 선택된 키로 API 호출 (페이지 번호 전달)

                CenterApiRequestDto requestDto = CenterApiRequestDto.from(1, getPageSize());
                CenterApiResponseDto responseDto = centerApiService.fetchCenter(requestDto);

                List<CenterApiResponseDto.CenterDto> data =
                        responseDto.data().stream().toList();
                results.addAll(data);
                success = true;
            } catch (Exception e) {
                // 만약 호출 한도 초과(429) 등의 에러가 나면 키 교체
                System.out.println("키 에러 발생! 다음 키로 교체합니다: " + apiKeys.get(currentKeyIdx));
                currentKeyIdx++;
                if (currentKeyIdx >= apiKeys.size()) {
                    throw new RuntimeException("모든 API 키가 소진되었습니다.");
                }
            }
        }
    }
}
