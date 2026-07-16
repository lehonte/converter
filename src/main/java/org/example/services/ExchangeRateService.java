package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.connectors.NbrbConnector;
import org.example.dto.ExchangeRateResponseDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.exceptions.NullExchangeRatesException;
import org.example.repositiries.CurrenciesRepository;
import org.example.repositiries.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final NbrbConnector nbrbConnector;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrenciesRepository currenciesRepository;

    @Transactional
    public void dataLoading() {
        nbrbConnector.getNbrbRates(LocalDate.now())
                .forEach(nbrbRateDto -> {
                    Currencies currencies = currenciesRepository
                            .findByNbrbId(nbrbRateDto.nbrbId())
                            .orElse(null);

                    if (currencies == null) {
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

    }

    public ExchangeRateResponseDto getCurrencyPair(String code, LocalDate date) {
        if (date == null) date = LocalDate.now();
        ExchangeRates exchangeRates = exchangeRateRepository
                .findByCurrencyAndRateDate(currenciesRepository.findByCode(code).orElse(null), date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты не найден"));
        return ExchangeRateResponseDto.builder()
                .code(exchangeRates.getCurrency().getCode())
                .rate(exchangeRates.getRate())
                .date(exchangeRates.getRateDate())
                .build();

    }


    public List<ExchangeRateResponseDto> getAllCurrencies(LocalDate date) {
        return exchangeRateRepository
                .findByRateDate(date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты не найден"))
                .stream()
                .map(exchangeRates -> new ExchangeRateResponseDto(
                        exchangeRates.getCurrency().getCode(),
                        exchangeRates.getRate(),
                        exchangeRates.getRateDate()))
                .toList();
    }
}
