package com.back.domain.member.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_member_provider_providerId",
                    columnNames = {"type", "provider_id"})
        })
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @Column(nullable = false, length = 20)
    private String name;

    // 소셜은 email이 없을 수도 있어서 nullable 권장
    @Column(nullable = true)
    private String email;

    // 소셜은 password 없음
    @Column(nullable = true, length = 255)
    private String password;

    // 소셜은 rrn 없음
    @Column(nullable = true, length = 6)
    private Integer rrnFront;

    @Column(nullable = true, length = 1)
    private Integer rrnBackFirst;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 소셜 계정 식별자 (ex. 카카오 user id)
    // EMAIL 회원은 null
    @Column(name = "provider_id", nullable = true) // 단독 unique 제거 → (type, providerId)로 유니크 보장
    private String providerId;

    // 프로필 이미지 URL 컬럼 추가
    @Column(name = "profile_img_url", nullable = true)
    private String profileImgUrl;

    // 회원상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    public enum LoginType {
        EMAIL,
        NAVER,
        KAKAO
    }

    public enum Role {
        ADMIN,
        USER
    }

    public enum MemberStatus {
        PRE_REGISTERED, // 소셜 로그인만 완료(추가정보 미입력)
        ACTIVE // 필수 정보 입력 완료
    }

    // 외부에서 new 못 하게 막고, 생성 규칙을 한 곳에 모으는 생성자
    private Member(
            String name,
            String email,
            String password,
            Integer rrnFront,
            Integer rrnBackFirst,
            LoginType type,
            Role role,
            String providerId,
            String profileImgUrl,
            MemberStatus status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.rrnFront = rrnFront;
        this.rrnBackFirst = rrnBackFirst;
        this.type = type;
        this.role = role;
        this.providerId = providerId;
        this.profileImgUrl = profileImgUrl;
        this.status = status;

        // createdAt/modifiedAt을 현재 시각으로 초기화
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }

    // 이메일 회원가입 전용 생성 함수
    // - type은 EMAIL
    // - role은 USER
    // - createdAt/modifiedAt 자동 세팅
    public static Member createEmailUser(
            String name, String email, String encodedPassword, Integer rrnFront, Integer rrnBackFirst) {
        return new Member(
                name,
                email,
                encodedPassword,
                rrnFront,
                rrnBackFirst,
                LoginType.EMAIL,
                Role.USER,
                null,
                null,
                MemberStatus.ACTIVE);
    }

    // 소셜 회원 생성 (password/rrn 없음)
    public static Member createSocialUser(
            String name, String email, LoginType type, String providerId, String profileImgUrl) {
        return new Member(
                name,
                email, // 없으면 null 가능
                null, // password 없음
                null,
                null, // rrn 없음
                type,
                Role.USER,
                providerId,
                profileImgUrl,
                MemberStatus.PRE_REGISTERED);
    }

    // 소셜 로그인 시 프로필 동기화
    public void updateSocialProfile(String nickname, String profileImgUrl) {
        boolean changed = false;

        if (nickname != null && !nickname.isBlank() && !nickname.equals(this.name)) {
            this.name = nickname;
            changed = true;
        }

        if (profileImgUrl != null
                && !profileImgUrl.isBlank()
                && (this.profileImgUrl == null || !profileImgUrl.equals(this.profileImgUrl))) {
            this.profileImgUrl = profileImgUrl;
            changed = true;
        }

        if (changed) {
            touchModifiedAt();
        }
    }

    // 추가정보 입력 완료 처리 (소셜 PRE → ACTIVE)
    public void completeSocialSignup(Integer rrnFront, Integer rrnBackFirst) {
        // 필요한 값 검증은 서비스에서 해도 되고 여기서 해도 됨
        this.rrnFront = rrnFront;
        this.rrnBackFirst = rrnBackFirst;

        this.status = MemberStatus.ACTIVE;
        touchModifiedAt();
    }

    // 나중에 정보 수정할 때 modifiedAt 갱신용
    public void touchModifiedAt() {
        this.modifiedAt = LocalDateTime.now();
    }
}
