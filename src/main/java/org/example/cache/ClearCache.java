package org.example.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClearCache {

    private final RedisTemplate<String, Object> redisTemplate;

    public void clearExchangeRatesCache() {
        clearCache("rate:*");
        clearCache("rates-all:*");
        clearCache("conversion:*");
    }

    private void clearCache(String key) {

        ScanOptions options = ScanOptions.scanOptions()
                .match(key)
                .count(200)
                .build();

        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());

                if (keys.size() >= 500) {
                    redisTemplate.unlink(keys);
                    keys.clear();
                }
            }

            if (!keys.isEmpty()) {
                redisTemplate.unlink(keys);
                keys.clear();
            }
        }
    }
}
