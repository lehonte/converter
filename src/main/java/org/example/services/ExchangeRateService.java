package org.example.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.example.connectors.NbrbConnector;
import org.example.dto.ExchangeRateResponseDto;
import org.example.dto.NbrbRateDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.exceptions.CurrencyNotFoundException;
import org.example.exceptions.NullExchangeRatesException;
import org.example.exceptions.SecondDataIsEarlierException;
import org.example.mappers.ExchangeRatesMapper;
import org.example.repositories.CurrenciesRepository;
import org.example.repositories.ExchangeRateRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final NbrbConnector nbrbConnector;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrenciesRepository currenciesRepository;
    private final DataLoadingTransaction dataLoadingTransaction;
    private final ProducerTemplate producerTemplate;
    private final ExchangeRatesMapper exchangeRatesMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public void dataLoading() {
        log.info("Начало загрузки курсов из НБРБ");
        List<NbrbRateDto> rates = nbrbConnector.getNbrbRates(LocalDate.now());
        log.info("Конец загрузки курсов из НБРБ, было загружено {}", rates.size());
        dataLoadingTransaction.dataLoadingTransaction(rates);
        redisTemplate.delete("exchangeRates");
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);

    }

    public void dataLoadingWithCamel() {
        log.info("Загрузка курсов из НБРБ с помощью Camel");
        producerTemplate.sendBody("direct:startNbrbRoute", null);
        log.info("Маршрут Camel успешно отработал");
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);

    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getCurrencyPair(String code, LocalDate rateDate) {

        rateDate = getDate(rateDate);
        ExchangeRates cache = (ExchangeRates) redisTemplate.opsForValue().get(code + rateDate);

        if (cache != null)  return exchangeRatesMapper.toExchangeRateResponseDto(cache);

        Currencies currency = getCurrency(code);
        ExchangeRates exchangeRates = getRate(rateDate, currency);

        redisTemplate.opsForValue().set(code + rateDate, exchangeRates, Duration.ofDays(1));

        return exchangeRatesMapper.toExchangeRateResponseDto(exchangeRates);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponseDto> getAllCurrencies(LocalDate rateDate) {

        rateDate = getDate(rateDate);
        List<ExchangeRates> cache = (List<ExchangeRates>) redisTemplate.opsForValue().get(String.valueOf(rateDate));

        if (cache != null)  return exchangeRatesMapper.toExchangeRateResponseDtoList(cache);

        List<ExchangeRates> rates = exchangeRateRepository.findByRateDate(rateDate);
        if (rates.isEmpty()) throw new NullExchangeRatesException("Курс валюты не найден");

        redisTemplate.opsForValue().set(String.valueOf(rateDate), rates, Duration.ofDays(1));

        return exchangeRatesMapper.toExchangeRateResponseDtoList(rates);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponseDto> getAllCurrenciesForTime(String code, LocalDate fromDate, LocalDate toDate)
            throws SecondDataIsEarlierException, CurrencyNotFoundException, NullExchangeRatesException {

        Currencies currency = getCurrency(code);
        if (toDate.isBefore(fromDate)) throw new SecondDataIsEarlierException("Некорректный диапазон дат: вторая граница раньше первой");

        List<ExchangeRates> rates = exchangeRateRepository.findByCurrencyAndRateDateBetween(currency, fromDate, toDate);
        if (rates.isEmpty()) throw new NullExchangeRatesException("Курс валюты не найден");

        return  exchangeRatesMapper.toExchangeRateResponseDtoList(rates);
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getExchangeRateBetweenTwoCurrencies(String firstCode, String secondCode, LocalDate rateDate) {
        rateDate = getDate(rateDate);

        ExchangeRateResponseDto cache = (ExchangeRateResponseDto) redisTemplate.opsForValue().get(firstCode + secondCode + rateDate);
        if (cache != null) return cache;

        Currencies firstCurrency = getCurrency(firstCode);
        Currencies secondCurrency = getCurrency(secondCode);

        ExchangeRates firstRate = getRate(rateDate, firstCurrency);
        ExchangeRates secondRate = getRate(rateDate, secondCurrency);
        BigDecimal newRate = getNewRate(firstRate, secondRate);

        ExchangeRateResponseDto result = ExchangeRateResponseDto.builder()
                .scale(1L)
                .code(firstRate.getCurrency().getCode() +"/"+ secondRate.getCurrency().getCode())
                .rate(newRate)
                .rateDate(firstRate.getRateDate())
                .build();

        redisTemplate.opsForValue().set(firstCode + secondCode + rateDate, result, Duration.ofDays(1));

        return result;
    }

    private static BigDecimal getNewRate(ExchangeRates firstRate, ExchangeRates secondRate) {
        return (firstRate.getRate().multiply(BigDecimal.valueOf(secondRate.getScale())))
                .divide(secondRate.getRate().multiply(BigDecimal.valueOf(firstRate.getScale())),
                        6, RoundingMode.HALF_UP);
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
