package com.back.domain.member.member.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @Column(nullable = false, length = 6)
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
    private LoginType type;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 소셜 계정 식별자 (ex. 카카오 user id)
    // EMAIL 회원은 null
    @Column(nullable = true) // 단독 unique 제거 → (type, providerId)로 유니크 보장
    private String providerId;

    public enum LoginType {
        EMAIL,
        NAVER,
        KAKAO
    }

    public enum Role {
        ADMIN,
        USER
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
            String providerId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.rrnFront = rrnFront;
        this.rrnBackFirst = rrnBackFirst;
        this.type = type;
        this.role = role;
        this.providerId = providerId;

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
        return new Member(name, email, encodedPassword, rrnFront, rrnBackFirst, LoginType.EMAIL, Role.USER, null);
    }

    // 소셜 회원 생성 (password/rrn 없음)
    public static Member createSocialUser(String name, String email, LoginType type, String providerId) {
        return new Member(
                name,
                email, // 없으면 null 가능
                null, // password 없음
                null,
                null, // rrn 없음
                type,
                Role.USER,
                providerId);
    }

    // 나중에 정보 수정할 때 modifiedAt 갱신용
    public void touchModifiedAt() {
        this.modifiedAt = LocalDateTime.now();
    }
}
