package com.back.domain.welfare.estate.controller;

import java.util.List;

import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.estate.dto.EstateSearchResonseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.service.EstateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/welfare/estate")
@RequiredArgsConstructor
@Slf4j
public class EstateController {
    private final EstateService estateService;

    @GetMapping("/location")
    public EstateSearchResonseDto getEstateLocation(@RequestParam String sido, @RequestParam String signguNm) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Estate> estateList = estateService.searchEstateLocation(sido, signguNm);

        stopWatch.stop();
        log.info("[캐시 적용] 행복주택 검색 실행 시간: {} ms", stopWatch.getTotalTimeMillis());

        return new EstateSearchResonseDto(estateList);
    }

    @GetMapping("/location/no-cache")
    public EstateSearchResonseDto getEstateLocationNoCache(@RequestParam String sido, @RequestParam String signguNm) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Estate> estateList = estateService.searchEstateLocationNoCache(sido, signguNm);

        stopWatch.stop();
        log.info("[캐시 미적용] 행복주택 검색 실행 시간: {} ms", stopWatch.getTotalTimeMillis());

        return new EstateSearchResonseDto(estateList);
    }
}
