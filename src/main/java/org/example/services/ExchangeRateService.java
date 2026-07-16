package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.connectors.NbrbConnector;
import org.example.dto.ExchangeRateResponseDto;
import org.example.dto.NbrbRateDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.exceptions.CurrencyNotFoundException;
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
    private final DataLoadingTransaction dataLoadingTransaction;

    public void dataLoading() {
        List<NbrbRateDto> rates = nbrbConnector.getNbrbRates(LocalDate.now());
        dataLoadingTransaction.dataLoadingTransaction(rates);
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getCurrencyPair(String code, LocalDate date) {
        if (date == null) date = LocalDate.now();

        Currencies currency = currenciesRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта кода не найдена"));

        ExchangeRates exchangeRates = exchangeRateRepository
                .findByCurrencyAndRateDate(currency, date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты не найден"));
        return ExchangeRateResponseDto.builder()
                .code(exchangeRates.getCurrency().getCode())
                .rate(exchangeRates.getRate())
                .date(exchangeRates.getRateDate())
                .build();

    }


    @Transactional(readOnly = true)
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
