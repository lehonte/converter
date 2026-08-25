package org.example.cache;

import lombok.RequiredArgsConstructor;
import org.redisson.RedissonRateLimiter;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RateLimiting {

    private final long MAX_LIFECYCLE  = 1;
    private final int MAX_REQUESTS = 50;

    private final RedissonClient redissonClient;

    public boolean rateLimitingToApi() {

        RRateLimiter rRateLimiter = redissonClient.getRateLimiter("limit:request:NBRB");
        rRateLimiter.trySetRate(RateType.OVERALL, MAX_REQUESTS, Duration.ofMinutes(MAX_LIFECYCLE));

        return rRateLimiter.tryAcquire(1); //вернет true если есть место (не сделали 50 запросов в минуту) и следит за жц хранимых запросов

//это то как работает под капотом по сути, но без предусмотрения многопоточности, поэтому стоит исправить чтобы не было гонки потоков
//        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
//        String key = "limit:request:NBRB";
//        long now = System.currentTimeMillis();
//
//        zSet.removeRangeByScore(key, 0, now - MAX_LIFECYCLE );
//        Long currentCount = zSet.zCard(key);
//
//        if (currentCount != null && currentCount >= MAX_REQUESTS) return false;
//
//        String uniqueValue = now + "-" + UUID.randomUUID();
//        zSet.add(key, uniqueValue, now );
//        redisTemplate.expire(key, Duration.ofMinutes(5));
//
//        return true;
    }
}
