package com.back.domain.welfare.estate.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.back.domain.welfare.estate.dto.EstateSearchResonseDto;
import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.service.EstateService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/welfare/estate")
@RequiredArgsConstructor
public class EstateController {
    private EstateService estateService;

    @GetMapping("/location")
    public EstateSearchResonseDto getEstateLocation(@RequestParam String sido) {
        List<Estate> estateList = estateService.searchEstateLocation(sido);

        return new EstateSearchResonseDto(estateList);
    }
}
