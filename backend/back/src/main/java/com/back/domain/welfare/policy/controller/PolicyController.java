package com.back.domain.welfare.policy.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.policy.document.PolicyDocument;
import com.back.domain.welfare.policy.dto.PolicyElasticSearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicyFetchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchRequestDto;
import com.back.domain.welfare.policy.dto.PolicySearchResponseDto;
import com.back.domain.welfare.policy.search.PolicySearchCondition;
import com.back.domain.welfare.policy.service.PolicyElasticSearchService;
import com.back.domain.welfare.policy.service.PolicyFetchService;
import com.back.domain.welfare.policy.service.PolicyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/welfare/policy")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {
    private final PolicyService policyService;
    private final PolicyFetchService policyFetchService;
    private final PolicyElasticSearchService policyElasticSearchService;

    @GetMapping("/search")
    public List<PolicyDocument> search(PolicyElasticSearchRequestDto policyElasticSearchRequestDto) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        PolicySearchCondition condition = PolicySearchCondition.builder()
                .keyword(policyElasticSearchRequestDto.keyword())
                .age(policyElasticSearchRequestDto.age())
                .earn(policyElasticSearchRequestDto.earn())
                .regionCode(policyElasticSearchRequestDto.regionCode())
                .jobCode(policyElasticSearchRequestDto.jobCode())
                .schoolCode(policyElasticSearchRequestDto.schoolCode())
                .marriageStatus(policyElasticSearchRequestDto.marriageStatus())
                .keywords(policyElasticSearchRequestDto.keywords())
                .build();
        List<PolicyDocument> result = policyElasticSearchService.search(
                condition, policyElasticSearchRequestDto.from(), policyElasticSearchRequestDto.size());

        stopWatch.stop();
        log.info("[Elasticsearch] 정책 검색 실행 시간: {} ms", stopWatch.getTotalTimeMillis());

        return result;
    }

    @GetMapping("/search/db")
    public List<PolicySearchResponseDto> searchDb(PolicySearchRequestDto policySearchRequestDto) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<PolicySearchResponseDto> result = policyService.search(policySearchRequestDto);

        stopWatch.stop();
        log.info("[DB] 정책 검색 실행 시간: {} ms", stopWatch.getTotalTimeMillis());

        return result;
    }

    @GetMapping("/list")
    public String getPolicy() {
        PolicyFetchRequestDto requestDto = new PolicyFetchRequestDto(null, "1", "100", "json");

        CompletableFuture.runAsync(() -> {
            try {
                policyFetchService.fetchAndSavePolicies(requestDto);
                log.info("정책 데이터 적재 및 인덱싱 완료");
            } catch (IOException e) {
                log.error("정책 데이터 적재 실패", e);
            }
        });

        return "데이터 적재 작업이 백그라운드에서 시작되었습니다. 완료까지 시간이 걸릴 수 있습니다.";
    }
}
