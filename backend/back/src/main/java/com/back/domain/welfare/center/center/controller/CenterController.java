package com.back.domain.welfare.center.center.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.back.domain.welfare.center.center.dto.CenterSearchResponseDto;
import com.back.domain.welfare.center.center.entity.Center;
import com.back.domain.welfare.center.center.service.CenterService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/welfare/center")
@RequiredArgsConstructor
public class CenterController {
    private final CenterService centerService;

    @GetMapping("/location")
    public CenterSearchResponseDto getCenterList(@RequestParam String sido) {
        List<Center> estateList = centerService.getCenterData();

        return new CenterSearchResponseDto(estateList);
    }
}
