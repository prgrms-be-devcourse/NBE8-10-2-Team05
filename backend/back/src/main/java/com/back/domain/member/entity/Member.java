package com.back.domain.member.entity;

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
}
