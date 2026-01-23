package com.back.domain.member.member.dto;

public record LoginResponse(long memberId, String name, String accessToken) {}
