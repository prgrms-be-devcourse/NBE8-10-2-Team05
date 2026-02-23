package com.back.domain.member.member.dto;

import java.time.LocalDateTime;

import com.back.domain.member.member.entity.Member;

// 회원가입 성공 시 프론트에 내려줄 응답 형태
public record JoinResponse(
        Long id, String name, String email, Member.LoginType type, Member.Role role, LocalDateTime createdAt) {
    // 멤버 엔티티 → 응답 DTO 변환
    public static JoinResponse from(Member member) {
        return new JoinResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getType(),
                member.getRole(),
                member.getCreatedAt());
    }
}
