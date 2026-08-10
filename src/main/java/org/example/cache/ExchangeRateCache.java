package org.example.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExchangeRateCache {

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void deleteCache () {
        log.info("Очистка кеша");
    }
}
