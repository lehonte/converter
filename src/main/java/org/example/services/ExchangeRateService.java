package org.example.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.connectors.NbrbConnector;
import org.example.dto.ExchangeRateResponseDto;
import org.example.dto.NbrbRateDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.exceptions.CurrencyNotFoundException;
import org.example.exceptions.NullExchangeRatesException;
import org.example.exceptions.SecondDataIsEarlierException;
import org.example.repositiries.CurrenciesRepository;
import org.example.repositiries.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final NbrbConnector nbrbConnector;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrenciesRepository currenciesRepository;
    private final DataLoadingTransaction dataLoadingTransaction;

    public void dataLoading() {
        log.info("Начало загрузки курсов из НБРБ");
        List<NbrbRateDto> rates = nbrbConnector.getNbrbRates(LocalDate.now());
        log.info("Конец загрузки курсов из НБРБ, было загружено {}", rates.size());
        dataLoadingTransaction.dataLoadingTransaction(rates);
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getCurrencyPair(String code, LocalDate date) {
        if (date == null) date = LocalDate.now();

        Currencies currency = currenciesRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта '" + code + "' не найдена"));

        ExchangeRates exchangeRates = exchangeRateRepository
                .findByCurrencyAndRateDate(currency, date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты не найден"));
        return ExchangeRateResponseDto.builder()
                .code(exchangeRates.getCurrency().getCode() + "/BYN")
                .rate(exchangeRates.getRate())
                .scale(exchangeRates.getScale())
                .date(exchangeRates.getRateDate())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponseDto> getAllCurrencies(LocalDate date) {
        if (date == null) date = LocalDate.now();
        return exchangeRateRepository
                .findByRateDate(date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты не найден"))
                .stream()
                .map(exchangeRates -> new ExchangeRateResponseDto(
                        exchangeRates.getScale(),
                        exchangeRates.getCurrency().getCode() + "/BYN",
                        exchangeRates.getRate(),
                        exchangeRates.getRateDate()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponseDto> getAllCurrenciesForTime(String code, LocalDate fromDate, LocalDate toDate)
            throws SecondDataIsEarlierException, CurrencyNotFoundException, NullExchangeRatesException {
        Currencies currency = currenciesRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта '" + code + "' не найдена"));

        if (toDate.isBefore(fromDate)) {
            throw new SecondDataIsEarlierException("Неккоретный диапазон дат: вторая граница раньше первой");
        }

        return exchangeRateRepository.findByCurrencyAndRateDateBetween(currency, fromDate, toDate)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты не найден"))
                .stream()
                .map(exchangeRates -> new ExchangeRateResponseDto(
                        exchangeRates.getScale(),
                        exchangeRates.getCurrency().getCode() + "/BYN",
                        exchangeRates.getRate(),
                        exchangeRates.getRateDate()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getExchangeRateBetweenTwoCurrencies(String firstCode, String secondCode, LocalDate date) {
        if (date == null) date = LocalDate.now();

        Currencies firstCurrency = currenciesRepository.findByCode(firstCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта '" + firstCode + "' не найдена"));
        Currencies secondCurrency = currenciesRepository.findByCode(secondCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта '" + secondCode + "' не найдена"));

        ExchangeRates firstRate = exchangeRateRepository.findByCurrencyAndRateDate(firstCurrency, date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты '" + firstCurrency.getCode() + "' не найден"));
        ExchangeRates secondRate = exchangeRateRepository.findByCurrencyAndRateDate(secondCurrency, date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты '" + secondCurrency.getCode()+ "' не найден"));

        BigDecimal newRate = (firstRate.getRate().multiply(BigDecimal.valueOf(secondRate.getScale())))
                .divide(secondRate.getRate().multiply(BigDecimal.valueOf(firstRate.getScale())),
                        6, RoundingMode.HALF_UP);

        return ExchangeRateResponseDto.builder()
                .scale(1L)
                .code(firstRate.getCurrency().getCode() +"/"+ secondRate.getCurrency().getCode())
                .rate(newRate)
                .date(firstRate.getRateDate())
                .build();

    }
}
