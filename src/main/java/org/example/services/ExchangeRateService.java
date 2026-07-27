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
import org.example.repositories.CurrenciesRepository;
import org.example.repositories.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

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
        date = getDate(date);

        Currencies currency = getCurrency(code);
        ExchangeRates exchangeRates = getRate(date, currency);

        return ExchangeRateResponseDto.builder()
                .code(exchangeRates.getCurrency().getCode() + "/BYN")
                .rate(exchangeRates.getRate())
                .scale(exchangeRates.getScale())
                .date(exchangeRates.getRateDate())
                .build();
    }



    @Transactional(readOnly = true)
    public List<ExchangeRateResponseDto> getAllCurrencies(LocalDate date) {
        date = getDate(date);

        List<ExchangeRates> rates = exchangeRateRepository.findByRateDate(date);

        if (rates.isEmpty()) {
            throw new NullExchangeRatesException("Курс валюты не найден");
        }

        return  rates.stream()
                .map(getExchangeRateResponseDtoFunction())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponseDto> getAllCurrenciesForTime(String code, LocalDate fromDate, LocalDate toDate)
            throws SecondDataIsEarlierException, CurrencyNotFoundException, NullExchangeRatesException {

        Currencies currency = getCurrency(code);

        if (toDate.isBefore(fromDate)) {
            throw new SecondDataIsEarlierException("Некорректный диапазон дат: вторая граница раньше первой");
        }

        List<ExchangeRates> rates = exchangeRateRepository.findByCurrencyAndRateDateBetween(currency, fromDate, toDate);

        if (rates.isEmpty()) {
            throw new NullExchangeRatesException("Курс валюты не найден");
        }

        return  rates.stream()
                .map(getExchangeRateResponseDtoFunction())
                .toList();
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getExchangeRateBetweenTwoCurrencies(String firstCode, String secondCode, LocalDate date) {
        date = getDate(date);

        Currencies firstCurrency = getCurrency(firstCode);
        Currencies secondCurrency = getCurrency(secondCode);

        ExchangeRates firstRate = getRate(date, firstCurrency);
        ExchangeRates secondRate = getRate(date, secondCurrency);

        BigDecimal newRate = getNewRate(firstRate, secondRate);

        return ExchangeRateResponseDto.builder()
                .scale(1L)
                .code(firstRate.getCurrency().getCode() +"/"+ secondRate.getCurrency().getCode())
                .rate(newRate)
                .date(firstRate.getRateDate())
                .build();

    }

    private static BigDecimal getNewRate(ExchangeRates firstRate, ExchangeRates secondRate) {
        return (firstRate.getRate().multiply(BigDecimal.valueOf(secondRate.getScale())))
                .divide(secondRate.getRate().multiply(BigDecimal.valueOf(firstRate.getScale())),
                        6, RoundingMode.HALF_UP);
    }

    private static Function<ExchangeRates, ExchangeRateResponseDto> getExchangeRateResponseDtoFunction() {
        return exchangeRates -> new ExchangeRateResponseDto(
                exchangeRates.getScale(),
                exchangeRates.getCurrency().getCode() + "/BYN",
                exchangeRates.getRate(),
                exchangeRates.getRateDate());
    }

    private ExchangeRates getRate(LocalDate date, Currencies currency) {
        return exchangeRateRepository.findByCurrencyAndRateDate(currency, date)
                .orElseThrow(() -> new NullExchangeRatesException("Курс валюты '" + currency.getCode() + "' не найден"));
    }

    private LocalDate getDate(LocalDate date) {
        return date == null ? LocalDate.now() : date;
    }

    private Currencies getCurrency(String code) {
        return currenciesRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта '" + code + "' не найдена"));
    }
}
