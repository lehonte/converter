package org.example.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateLimiting {

    private final long MAX_LIFECYCLE  = 60000;
    private final int MAX_REQUESTS = 50;
    private final RedisTemplate<String, String> redisTemplate;

    public boolean rateLimitingToApi() {

        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
        String key = "limit:request:NBRB";
        long now = System.currentTimeMillis();

        zSet.removeRangeByScore("requestToApi", 0, now - MAX_LIFECYCLE );
        Long currentCount = zSet.zCard(key);

        if (currentCount != null && currentCount >= MAX_REQUESTS) return false;

        String uniqueValue = now + "-" + UUID.randomUUID();
        zSet.add(key, uniqueValue, now );
        redisTemplate.expire(key, Duration.ofMinutes(5));

        return true;
    }
}
