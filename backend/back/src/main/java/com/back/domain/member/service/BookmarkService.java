package com.back.domain.member.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.back.domain.member.entity.Bookmark;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.BookmarkRepository;
import com.back.domain.welfare.policy.entity.Policy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public List<Policy> getPolicies(Member member) {
        List<Bookmark> bookmarks = bookmarkRepository.getBookmarksByApplicantId(member.getId());

        List<Policy> policies = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            policies.add(bookmark.getPolicy());
        }

        return policies;
    }

    // 원래 북마크 목록에 policy가 있었는지를 리턴한다.
    public String changeBookmarkStatus(Member member, Policy policy) {

        List<Policy> policies = getPolicies(member);

        if (policies.contains(policy)) {
            policies.remove(policy);
            return "북마크가 해제되었습니다.";
        } else {
            policies.add(policy);
            return "북마크가 추가되었습니다.";
        }
    }
}
