package com.back.domain.member.cotroller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.member.dto.BookmarkPolicyResponseDto;
import com.back.domain.member.entity.Bookmark;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.BookmarkRepository;
import com.back.domain.member.service.BookmarkService;
import com.back.domain.welfare.policy.entity.Policy;

@RestController
@RequestMapping("/api/v1/member")
public class BookmarkController {
    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private BookmarkService bookmarkService;

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
