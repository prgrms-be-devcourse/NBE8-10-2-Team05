package com.back.domain.member.bookmark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.member.bookmark.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import com.back.domain.welfare.policy.entity.Policy;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> getBookmarksByApplicantId(Long applicantId);

    Optional<Bookmark> findByApplicantAndPolicy(Member member, Policy policy);
}
