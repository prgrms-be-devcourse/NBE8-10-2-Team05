package com.back.domain.member.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.dto.AddApplicationResponseDto;
import com.back.domain.member.dto.DeleteApplicationResponseDto;
import com.back.domain.member.entity.Application;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.service.ApplicationService;
import com.back.standard.util.Ut;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final MemberRepository memberRepository;

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    /**
     * JWT 토큰에서 Member 추출
     * @param authorizationHeader Authorization 헤더 값 (Bearer {token} 형식)
     * @return Member 엔티티, 토큰이 유효하지 않거나 멤버를 찾을 수 없으면 null
     */
    private Member extractMemberFromJwt(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String jwt = authorizationHeader.substring(7); // "Bearer " 제거

        // JWT 유효성 검증
        if (!Ut.Jwt.isValid(jwtSecretKey, jwt)) {
            return null;
        }

        // JWT payload 추출
        Map<String, Object> payload = Ut.Jwt.payload(jwtSecretKey, jwt);
        if (payload == null) {
            return null;
        }

        // memberId 추출
        Object memberIdObj = payload.get("memberId");
        if (memberIdObj == null) {
            return null;
        }

        Long memberId;
        if (memberIdObj instanceof Integer) {
            memberId = ((Integer) memberIdObj).longValue();
        } else if (memberIdObj instanceof Long) {
            memberId = (Long) memberIdObj;
        } else {
            return null;
        }

        // Member 조회
        return memberRepository.findById(memberId).orElse(null);
    }

    @GetMapping("/welfare-applications")
    public ResponseEntity<?> getApplicationList(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Member member = extractMemberFromJwt(authorizationHeader);

        if (member == null) {
            AddApplicationResponseDto responseDto =
                    new AddApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }

        List<Application> applications = applicationService.getApplicationList(member);
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/welfare-application/{policyId}")
    public ResponseEntity<AddApplicationResponseDto> addApplication(
            @PathVariable Integer policyId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Member member = extractMemberFromJwt(authorizationHeader);

        if (member == null) {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(addApplicationResponseDto);
        }

        Application application = applicationService.addApplication(member, policyId);

        if (application != null) {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.OK.value(), "저장되었습니다!");
            return ResponseEntity.ok(addApplicationResponseDto);
        } else {
            AddApplicationResponseDto addApplicationResponseDto =
                    new AddApplicationResponseDto(HttpStatus.NOT_FOUND.value(), "존재하지 않는 정책입니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(addApplicationResponseDto);
        }
    }

    @PutMapping("/welfare-application/{id}")
    public ResponseEntity<DeleteApplicationResponseDto> deleteAplication(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Member member = extractMemberFromJwt(authorizationHeader);

        if (member == null) {
            DeleteApplicationResponseDto deleteApplicationResponseDto =
                    new DeleteApplicationResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(deleteApplicationResponseDto);
        }

        DeleteApplicationResponseDto deleteApplicationResponseDto = applicationService.deleteApplication(member, id);

        return ResponseEntity.status(deleteApplicationResponseDto.getCode()).body(deleteApplicationResponseDto);
    }
}
