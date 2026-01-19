package com.back.domain.welfare.estate.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.back.domain.welfare.estate.entity.Estate;
import com.back.domain.welfare.estate.service.EstateService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/welfare/estate")
@RequiredArgsConstructor
public class EstateController {
    private EstateService estateService;

    @GetMapping("/list")
    public List<Estate> getEstateItems() {
        List<Estate> estateList = estateService.fetchEstateList();

        return estateList;
    }
}
