package com.back.domain.welfare.center.lawyer.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.center.lawyer.dto.LawyerReq;
import com.back.domain.welfare.center.lawyer.dto.LawyerRes;
import com.back.domain.welfare.center.lawyer.service.LawyerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/center/location/lawyer")
@RequiredArgsConstructor
public class LawyerController {
    private final LawyerService lawyerService;

    @GetMapping
    public ResponseEntity<Page<LawyerRes>> searchLawyersByDistrict(
            @Valid LawyerReq lawyerReq,
            @PageableDefault(size = 30, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<LawyerRes> lawyers = lawyerService.searchByDistrict(lawyerReq, pageable);

        return ResponseEntity.ok(lawyers);
    }
}
