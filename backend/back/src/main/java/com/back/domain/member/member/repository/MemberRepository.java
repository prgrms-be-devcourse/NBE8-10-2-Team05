package com.back.domain.member.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.member.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인/회원가입에서 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 회원가입에서 이메일 중복 체크
    boolean existsByEmail(String email);

    Optional<Member> findByTypeAndProviderId(Member.LoginType type, String providerId);
}
