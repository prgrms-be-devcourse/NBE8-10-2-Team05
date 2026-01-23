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

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 6)
    private Integer rrnFront;

    @Column(nullable = false, length = 1)
    private Integer rrnBackFirst;

    @Enumerated(EnumType.STRING)
    private LoginType type;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum LoginType {
        EMAIL,
        NAVER
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
            Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.rrnFront = rrnFront;
        this.rrnBackFirst = rrnBackFirst;
        this.type = type;
        this.role = role;

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
        return new Member(name, email, encodedPassword, rrnFront, rrnBackFirst, LoginType.EMAIL, Role.USER);
    }

    // 나중에 정보 수정할 때 modifiedAt 갱신용
    public void touchModifiedAt() {
        this.modifiedAt = LocalDateTime.now();
    }
}
