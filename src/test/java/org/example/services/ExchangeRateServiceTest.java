package org.example.services;

import org.example.connectors.NbrbConnector;
import org.example.dto.ExchangeRateResponseDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.exceptions.CurrencyNotFoundException;
import org.example.exceptions.NullExchangeRatesException;
import org.example.repositiries.CurrenciesRepository;
import org.example.repositiries.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private NbrbConnector nbrbConnector;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrenciesRepository currenciesRepository;

    @Mock
    private DataLoadingTransaction dataLoadingTransaction;

    //@InjectMocks или @BeforeEach как ниже. Второе явно
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateService(
                nbrbConnector, exchangeRateRepository, currenciesRepository, dataLoadingTransaction);
    }

    @Test
    void getCurrencyPairCurrencyExistsRateOnDataExsistsTest() {
        Currencies currency = new Currencies();
        when(currenciesRepository.findByCode("USD"))
                .thenReturn(Optional.of(currency));

        ExchangeRates exchangeRates = new ExchangeRates();
        exchangeRates.setCurrency(currency);
        exchangeRates.setRate(BigDecimal.valueOf(3.24));
        exchangeRates.setScale(1L);
        exchangeRates.setRateDate(LocalDate.now());
        when(exchangeRateRepository.findByCurrencyAndRateDate(currency, any(LocalDate.class)))
                .thenReturn(Optional.of(exchangeRates));

        ExchangeRateResponseDto result = exchangeRateService.getCurrencyPair("USD", LocalDate.now());

        assertThat(result.code()).isEqualTo("USD/BYN");
        assertThat(result.rate()).isEqualByComparingTo(BigDecimal.valueOf(3.24));
        assertThat(result.date()).isEqualTo(LocalDate.now());
    }

    @Test
    void getCurrencyPairCurrencyNotExistsTest() {
        when(currenciesRepository.findByCode("XYZ"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.getCurrencyPair("XYZ", LocalDate.now()))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void getCurrencyPairCurrencyExistsNotRateOnDateTest() {
        Currencies currency = new Currencies();
        when(currenciesRepository.findByCode("USD"))
                .thenReturn(Optional.of(currency));

        when(exchangeRateRepository.findByCurrencyAndRateDate(currency, any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.getCurrencyPair("USD", LocalDate.of(1800, 1, 1)))
                .isInstanceOf(NullExchangeRatesException.class);
    }

    @Test
    void getCurrencyPairNullDataTest() {
        Currencies currency = new Currencies();
        when(currenciesRepository.findByCode("USD"))
                .thenReturn(Optional.of(currency));

        ExchangeRates exchangeRates = new ExchangeRates();
        when(exchangeRateRepository.findByCurrencyAndRateDate(currency, any(LocalDate.class)))
                .thenReturn(Optional.of(exchangeRates));

        exchangeRateService.getCurrencyPair("USD", null);

        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(exchangeRateRepository).findByCurrencyAndRateDate(eq(currency), captor.capture());
        assertThat(captor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void getAllCurrenciesRatesExsistsOnDateTest() {
        Currencies currency = new Currencies();
        currency.setCode("USD");

        ExchangeRates mockRate = new ExchangeRates();
        mockRate.setCurrency(currency);
        mockRate.setScale(1L);
        mockRate.setRate(BigDecimal.valueOf(3.24));
        mockRate.setRateDate(LocalDate.now());

        List<ExchangeRates> exchangeRatesList = List.of(mockRate);

        when(exchangeRateRepository.findByRateDate(any(LocalDate.class)))
                .thenReturn(Optional.of(exchangeRatesList));

        List<ExchangeRateResponseDto> result = exchangeRateService.getAllCurrencies(LocalDate.now());

        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("USD/BYN");
        assertThat(result.get(0).rate()).isEqualByComparingTo(BigDecimal.valueOf(3.24));
        assertThat(result.get(0).date()).isEqualTo(LocalDate.now());
    }


    @Test
    void getAllCurrenciesRatesNotExistsOnDateTest() {
        when(exchangeRateRepository.findByRateDate(any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService.getAllCurrencies(LocalDate.of(1800, 1, 1)))
                .isInstanceOf(NullExchangeRatesException.class);
    }

    @Test
    void getExchangeRateBetweenTwoCurrencies () {
        Currencies firstCurrency = new Currencies();
        firstCurrency.setCode("USD");
        when(currenciesRepository.findByCode("USD"))
                .thenReturn(Optional.of(firstCurrency));

        Currencies secondCurrency = new Currencies();
        secondCurrency.setCode("EUR");
        when(currenciesRepository.findByCode("EUR"))
                .thenReturn(Optional.of(secondCurrency));

        ExchangeRates firstExchangeRates = new ExchangeRates();
        firstExchangeRates.setCurrency(firstCurrency);
        firstExchangeRates.setRate(BigDecimal.valueOf(3.24));
        firstExchangeRates.setScale(1L);
        firstExchangeRates.setRateDate(LocalDate.now());

        when(exchangeRateRepository.findByCurrencyAndRateDate(secondCurrency, LocalDate.now()))
                .thenReturn(Optional.of(firstExchangeRates));

        ExchangeRates secondExchangeRates = new ExchangeRates();
        secondExchangeRates.setCurrency(secondCurrency);
        secondExchangeRates.setRate(BigDecimal.valueOf(3.3));
        secondExchangeRates.setScale(1L);
        secondExchangeRates.setRateDate(LocalDate.now());

        when(exchangeRateRepository.findByCurrencyAndRateDate(secondCurrency, LocalDate.now()))
                .thenReturn(Optional.of(secondExchangeRates));

        ExchangeRateResponseDto result = exchangeRateService
                .getExchangeRateBetweenTwoCurrencies("USD", "EUR", LocalDate.now());

        assertThat(result.code()).isEqualTo("USD/EUR");
        assertThat(result.rate()).isEqualByComparingTo(BigDecimal.valueOf(0.981818));
        assertThat(result.date()).isEqualTo(LocalDate.now());
    }

    @Test
    void getExchangeRateBetweenTwoCurrenciesFirstCurrencyNotExsistsTest() {
        when(currenciesRepository.findByCode("XCV"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService
                .getExchangeRateBetweenTwoCurrencies("XCV", null, LocalDate.now()))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("XCV");
    }

    @Test
    void getExchangeRateBetweenTwoCurrenciesSecondCurrencyNotExsistsTest() {
        Currencies firstCurrency = new Currencies();
        firstCurrency.setCode("USD");
        when(currenciesRepository.findByCode("USD"))
                .thenReturn(Optional.of(firstCurrency));

        when(currenciesRepository.findByCode("XCV"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService
                .getExchangeRateBetweenTwoCurrencies("USD", "XCV", LocalDate.now()))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("XCV");
    }

    @Test
    void getExchangeRateBetweenTwoCurrenciesOnDateNotExistsTest() {
        Currencies firstCurrency = new Currencies();
        firstCurrency.setCode("USD");
        when(currenciesRepository.findByCode("USD"))
                .thenReturn(Optional.of(firstCurrency));

        Currencies secondCurrency = new Currencies();
        secondCurrency.setCode("EUR");
        when(currenciesRepository.findByCode("EUR"))
                .thenReturn(Optional.of(secondCurrency));

        when(exchangeRateRepository.findByCurrencyAndRateDate(firstCurrency, any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(exchangeRateRepository.findByCurrencyAndRateDate(secondCurrency, any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> exchangeRateService
                .getExchangeRateBetweenTwoCurrencies("USD", "EUR", LocalDate.of(1800, 1, 1)))
                .isInstanceOf(NullExchangeRatesException.class);
    }


}