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
            // principal에 memberId를 넣어둔 상태라서 이렇게 꺼내면 됨
            memberId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            // 혹시 String으로 들어오는 경우 대비
            memberId = Long.valueOf(String.valueOf(auth.getPrincipal()));
        }

        // TODO: authentication을 뚫고 securityContextHolde도 뚫고 여기까지 왔는데
        //       그냥 principal에서 가져온 정보로는 안되는 건가요?
        //       단순 조회(글 목록 보기 등): JWT 내부의 principal 정보만 믿고 DB 안 갑니다.
        //       결제, 회원수정, 개인정보: 이때만 getActor()를 통해 DB에서 최신 정보를 확인합니다.

        // get actor같은 연할을 하는 곳인데 강사님은 DB조회를 안하고 나는 DB조희를 함
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new ServiceException("MEMBER-404", "존재하지 않는 회원입니다."));
    }
}
