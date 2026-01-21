package com.back.domain.member.dto;

public record LoginResponse(long memberId, String name, String accessToken) {}
