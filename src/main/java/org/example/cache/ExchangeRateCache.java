package org.example.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExchangeRateCache {

    @Caching(evict = {
            @CacheEvict(value = "exchangeRates", allEntries = true),
            @CacheEvict(value = "exchangeRatesAll", allEntries = true),
            @CacheEvict(value = "exchangeRatesConversion", allEntries = true)
    })
    public void deleteCache () {
        log.info("Очистка кеша");
    }
}
