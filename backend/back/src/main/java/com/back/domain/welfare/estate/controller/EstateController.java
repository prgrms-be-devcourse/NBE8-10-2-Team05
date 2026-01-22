package com.back.domain.welfare.estate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.back.domain.welfare.estate.dto.EstateFetchResponseDto;
import com.back.domain.welfare.estate.service.EstateService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/welfare/estate")
@RequiredArgsConstructor
public class EstateController {
    private EstateService estateService;

    @GetMapping("/list")
    public EstateFetchResponseDto getEstateItems() {
        estateService.fetchEstateList();

        return null;
    }
}
