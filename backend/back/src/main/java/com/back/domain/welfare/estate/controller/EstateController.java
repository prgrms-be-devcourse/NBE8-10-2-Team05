package com.back.domain.welfare.estate.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.estate.dto.EstateRegionDto;
import com.back.domain.welfare.estate.dto.EstateSearchResonseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.entity.EstateRegionCache;
import com.back.domain.welfare.estate.service.EstateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/estate")
@RequiredArgsConstructor
public class EstateController {
    private final EstateService estateService;
    private final EstateRegionCache regionCache;

    @GetMapping("/location")
    public EstateSearchResonseDto getEstateLocation(@RequestParam String sido, @RequestParam String signguNm) {
        List<Estate> estateList = estateService.searchEstateLocation(sido, signguNm);

        return new EstateSearchResonseDto(estateList);
    }

    @GetMapping("/regions")
    public List<EstateRegionDto> getEstateRegions() {
        return regionCache.getRegionList();
    }
}
