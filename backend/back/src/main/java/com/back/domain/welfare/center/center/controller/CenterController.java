package com.back.domain.welfare.center.center.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.back.domain.welfare.center.center.service.CenterService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/welfare/center")
@RequiredArgsConstructor
public class CenterController {
    private final CenterService centerService;

    @GetMapping("/list")
    public void getCenterList(@RequestParam String sido) {}
}
