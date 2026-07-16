package org.example.shedulers;

import lombok.RequiredArgsConstructor;
import org.example.services.ExchangeRateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateScheduler {

    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "0 0 0 * * *")
    public void rateSceduler() {
        exchangeRateService.dataLoading();
    }
}
