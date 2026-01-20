package com.back.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.dto.MemberDetailReq;
import com.back.domain.member.dto.MemberDetailRes;
import com.back.domain.member.service.MemberDetailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/detail")
@RequiredArgsConstructor
public class MemberDetailController {
    private final MemberDetailService memberDetailService;

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDetailRes> getMemberDetail(@PathVariable Long memberId) {
        MemberDetailRes response = memberDetailService.getDetail(memberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberDetailRes> getMemberDetail(
            @PathVariable Long memberId, @Valid @RequestBody MemberDetailReq reqBody) {
        memberDetailService.modify(memberId, reqBody);

        MemberDetailRes response = memberDetailService.getDetail(memberId);
        return ResponseEntity.ok(response);
    }
}
