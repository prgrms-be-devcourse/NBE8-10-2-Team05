package com.back.global.redis;

import org.springframework.data.redis.core.index.Indexed;

import jakarta.persistence.Id;
import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RedisEntity {
    @Id
    @Indexed
    private String id;

    private String nickname;

    @Setter
    private String apiKey;
}
