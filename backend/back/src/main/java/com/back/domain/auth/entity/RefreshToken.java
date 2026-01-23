package com.back.domain.auth.entity;

import java.time.LocalDateTime;

import com.back.domain.member.entity.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 엔티티
 *
 * 역할:
 * - Access Token이 만료되었을 때
 * - 새 Access Token을 발급받기 위해 사용하는 토큰
 *
 * 핵심 포인트:
 * - refresh token "원문"은 DB에 저장하지 않고
 * - 보안을 위해 hash 값만 저장한다
 */
@Entity // JPA 엔티티임을 선언 (이 클래스는 DB 테이블과 매핑됨)
@Table(name = "refresh_token") // DB 테이블 이름 지정
@Getter // 모든 필드에 getter 자동 생성
@NoArgsConstructor // JPA가 객체를 만들 때 필요한 기본 생성자
public class RefreshToken {

    /**
     * PK (기본 키)
     * - refresh_token 테이블의 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 회원의 refresh token인지
     *
     * 관계:
     * - Member 1 : RefreshToken N
     * - 한 회원은 여러 번 로그인할 수 있으므로 여러 refresh token을 가질 수 있다
     *
     * fetch = LAZY:
     * - refresh token 조회 시 member 정보는 바로 가져오지 않는다
     * - member가 실제로 필요할 때만 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * refresh token의 해시 값
     *
     * 이유:
     * - 실제 refresh token(UUID 문자열)을 그대로 DB에 저장하면 보안 위험
     * - 그래서 SHA-256 같은 방식으로 해시한 값만 저장
     *
     * length = 64:
     * - SHA-256을 hex 문자열로 만들면 길이가 64
     */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    /**
     * refresh token이 생성된 시각
     * - 토큰 발급 시점 기록용
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * refresh token 만료 시각
     * - 이 시간이 지나면 refresh token은 더 이상 사용할 수 없다
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * refresh token 폐기 시각
     *
     * 사용 예:
     * - 로그아웃 시
     * - 보안 이슈로 강제 만료 시
     *
     * 규칙:
     * - null이면 아직 유효
     * - 값이 있으면 이미 폐기된 토큰
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * 생성자 (외부에서 new 못 하게 private)
     *
     * Member 엔티티에서 쓰는 패턴과 동일:
     * - 생성 규칙을 한 곳에 모으기 위해 사용
     */
    private RefreshToken(Member member, String tokenHash, LocalDateTime expiresAt) {
        this.member = member;
        this.tokenHash = tokenHash;

        // 생성 시점 기준으로 시간 자동 세팅
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.expiresAt = expiresAt;
        this.revokedAt = null; // 처음 생성될 때는 아직 폐기되지 않음
    }

    /**
     * RefreshToken 생성 전용 팩토리 메서드
     *
     * 역할:
     * - refresh token 생성 규칙을 통일
     * - new 키워드를 외부에서 쓰지 않게 하기 위함
     */
    public static RefreshToken create(Member member, String tokenHash, LocalDateTime expiresAt) {
        return new RefreshToken(member, tokenHash, expiresAt);
    }

    /**
     * refresh token을 폐기 처리
     * - 로그아웃 시 호출
     * - revokedAt에 현재 시각을 기록
     */
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * 이미 폐기된 토큰인지 확인
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * 만료된 토큰인지 확인
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 현재 사용 가능한 refresh token인지 확인
     *
     * 조건:
     * - 폐기되지 않았고
     * - 만료되지 않았을 것
     */
    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }
}
