package com.back.standard.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActorProvider {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ServiceException("AUTH-401", "인증 정보가 없습니다.");
        }

        Long memberId;
        try {
            memberId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            memberId = Long.valueOf(String.valueOf(auth.getPrincipal()));
        }

        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));
    }
}
