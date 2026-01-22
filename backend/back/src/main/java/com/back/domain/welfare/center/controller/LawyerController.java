package com.back.domain.welfare.center.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.welfare.center.dto.LawyerRes;
import com.back.domain.welfare.center.service.LawyerService;
import com.back.global.geo.entity.AddressDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/welfare/center//location/lawyer")
@RequiredArgsConstructor
public class LawyerController {
    private final LawyerService lawyerService;

    @GetMapping
    public ResponseEntity<List<LawyerRes>> searchLawyersByDistrict(@Valid AddressDto addressDto) {
        List<LawyerRes> lawyers = lawyerService.getFilteredLawyers(addressDto);

        return ResponseEntity.ok(lawyers);
    }
}
