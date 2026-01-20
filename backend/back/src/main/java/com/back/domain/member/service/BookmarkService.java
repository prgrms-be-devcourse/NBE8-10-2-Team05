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

    public List<Policy> getBookmarks(Member member) {
        List<Bookmark> bookmarks = bookmarkRepository.getBookmarksByApplicantId(member.getId());

        List<Policy> policies = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            policies.add(bookmark.getPolicy());
        }

        return policies;
    }
}
