package com.back.domain.member.bookmark.controller;

import java.util.List;

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
import com.back.standard.util.ActorProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkService bookmarkService;
    private final MemberRepository memberRepository;
    private final PolicyRepository policyRepository;
    private final ActorProvider actorProvider;

    @GetMapping("/welfare-bookmarks")
    public ResponseEntity<BookmarkPolicyResponseDto> getBookmarks() {

        Member member = actorProvider.getActor();

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
    public ResponseEntity<BookmarkUpdateResponseDto> updateBookmark(@PathVariable int policyId) {
        Member member = actorProvider.getActor();
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
