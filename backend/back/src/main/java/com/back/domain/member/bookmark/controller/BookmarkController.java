package com.back.domain.member.bookmark.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.back.domain.member.bookmark.dto.BookmarkPolicyResponseDto;
import com.back.domain.member.bookmark.dto.BookmarkUpdateResponseDto;
import com.back.domain.member.bookmark.repository.BookmarkRepository;
import com.back.domain.member.bookmark.service.BookmarkService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.domain.welfare.policy.repository.PolicyRepository;
import com.back.standard.util.Ut;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkService bookmarkService;
    private final MemberRepository memberRepository;
    private final PolicyRepository policyRepository;

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

        long memberId;
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

    @GetMapping("/welfare-bookmarks")
    public ResponseEntity<BookmarkPolicyResponseDto> getBookmarks(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Member member = extractMemberFromJwt(authorizationHeader);

        if (member == null) {
            BookmarkPolicyResponseDto responseDto =
                    new BookmarkPolicyResponseDto(HttpStatus.UNAUTHORIZED.value(), "로그인 후 이용해주세요", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }

        List<Policy> policies = bookmarkService.getPolicies(member);

        BookmarkPolicyResponseDto response = new BookmarkPolicyResponseDto(200, "", policies);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/welfare-bookmarks/{policyId}")
    public ResponseEntity<BookmarkUpdateResponseDto> updateBookmark(
            @RequestParam int policyId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Member member = extractMemberFromJwt(authorizationHeader);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // policyId로 policy 가져오기
        Policy policy = policyRepository.findById(policyId).orElse(null);

        if (policy == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // policy-member가 북마크에 연동 되어있는지 확인
        String message = bookmarkService.changeBookmarkStatus(member, policy);

        BookmarkUpdateResponseDto responseDto = new BookmarkUpdateResponseDto(200, message);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
