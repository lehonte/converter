package org.example.shedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.services.ExchangeRateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateScheduler {

    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "${nbrb.converter.shedulers.cron}")
    public void rateScheduler() {
        log.info("Загрузка данных по расписанию началась");
        try {
            exchangeRateService.dataLoading();
        } catch (Exception e) {
            log.error("Что-то пошло не так с загрузкой курсов по расписанию", e);
        }
    }
}
