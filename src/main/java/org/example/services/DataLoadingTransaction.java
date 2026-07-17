package org.example.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.NbrbRateDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.repositiries.CurrenciesRepository;
import org.example.repositiries.ExchangeRateRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoadingTransaction {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrenciesRepository currenciesRepository;

    @Transactional
    public void dataLoadingTransaction(List<NbrbRateDto> rates) {
        log.info("Начало транзакции новых курсов");
        rates.forEach(nbrbRateDto -> {
            Currencies currencies = currenciesRepository
                    .findByNbrbId(nbrbRateDto.nbrbId())
                    .orElse(null);

            if (currencies == null) {
                log.info("Добавлена новая валюта: {} ({})", nbrbRateDto.code(), nbrbRateDto.name());
                currencies = new Currencies();
                currencies.setCode(nbrbRateDto.code());
                currencies.setName(nbrbRateDto.name());
                currencies.setNbrbId(nbrbRateDto.nbrbId());
                currencies = currenciesRepository.save(currencies);
            }

            ExchangeRates exchangeRates = exchangeRateRepository
                    .findByCurrencyAndRateDate(currencies, nbrbRateDto.date())
                    .orElse(null);

            if (exchangeRates == null) {
                exchangeRates = new ExchangeRates();
                exchangeRates.setCurrency(currencies);
            }

            exchangeRates.setRate(nbrbRateDto.rate());
            exchangeRates.setRateDate(nbrbRateDto.date());
            exchangeRates.setScale(nbrbRateDto.scale());
            exchangeRateRepository.save(exchangeRates);
        });
        log.info("Конец транзакции новых курсов");
    }
}
