package com.back.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.back.domain.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // refresh token hash로 조회 (reissue에서 사용)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // 로그아웃 시 회원의 refresh 토큰 전부 삭제/폐기용
    void deleteByMember_Id(Long memberId);
}
