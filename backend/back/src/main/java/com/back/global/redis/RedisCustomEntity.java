package com.back.global.redis;

import org.springframework.data.redis.core.RedisHash;

import lombok.*;

@Builder
@RedisHash("example")
public record RedisCustomEntity(Integer id, String nickname, String apiKey) {}
