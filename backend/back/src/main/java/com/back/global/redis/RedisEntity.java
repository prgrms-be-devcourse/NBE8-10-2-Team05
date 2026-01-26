package com.back.global.redis;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@RedisHash(value = "redis", timeToLive = 3600)
public class RedisEntity {
    @Id
    @Indexed
    private String id;

    private String nickname;

    @Setter
    private String apiKey;
}
