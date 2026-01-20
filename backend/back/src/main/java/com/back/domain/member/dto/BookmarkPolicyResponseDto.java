package com.back.domain.member.dto;

import java.util.List;

import com.back.domain.welfare.policy.entity.Policy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookmarkPolicyResponseDto {
    private int code;
    private List<Policy> policies;

    public BookmarkPolicyResponseDto(int code, List<Policy> policies) {
        this.code = code;
        this.policies = policies;
    }
}
