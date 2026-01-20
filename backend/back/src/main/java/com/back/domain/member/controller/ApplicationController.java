package com.back.domain.member.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.member.service.ApplicationService;

@RestController
@RequestMapping("/api/v1/member")
public class ApplicationController {

    private ApplicationService applicationService;
}
