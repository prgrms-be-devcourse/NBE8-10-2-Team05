package com.back.domain.member.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.member.dto.BookmarkPolicyResponseDto;
import com.back.domain.member.entity.Bookmark;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.BookmarkRepository;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.service.BookmarkService;
import com.back.domain.welfare.policy.entity.Policy;
import com.back.standard.util.Ut;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkService bookmarkService;
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

    @GetMapping("/welfare-bookmarks")
    public ResponseEntity<BookmarkPolicyResponseDto> getBookmarks() {

        Member member = new Member(); // TODO : jwt로 멤버 정보 가지고 오기
        // TODO: JWT 인증 기능 구현 전까지는 임시 처리
        if (member.getId() == null) {
            // 인증 기능 구현 전까지 빈 리스트 반환
            return ResponseEntity.ok(new BookmarkPolicyResponseDto(200, new ArrayList<>()));
        }

        List<Bookmark> bookmarks = bookmarkRepository.getBookmarksByApplicantId(member.getId());

        List<Policy> policies = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            policies.add(bookmark.getPolicy());
        }

        BookmarkPolicyResponseDto response = new BookmarkPolicyResponseDto(200, policies);

        return ResponseEntity.ok(response);
    }
}
