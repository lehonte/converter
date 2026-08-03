package org.example.services;

import org.example.dto.NbrbRateDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.repositories.CurrenciesRepository;
import org.example.repositories.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataLoadingTransactionTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrenciesRepository currenciesRepository;

    @InjectMocks
    private DataLoadingTransaction dataLoadingTransaction;

    @Test
    void setDataLoadingTransactionNewCurrencyTest() {

        NbrbRateDto nbrbRateDto = new NbrbRateDto("USD", 123L, "Dollar", LocalDate.now(), 1L, new BigDecimal(1));
        List<NbrbRateDto> nbrbRateDtoList = List.of(nbrbRateDto);

        when(currenciesRepository.findByNbrbId(123L)).thenReturn(Optional.empty());

        when(currenciesRepository.save(any(Currencies.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataLoadingTransaction.dataLoadingTransaction(nbrbRateDtoList);

        ArgumentCaptor<Currencies> captor = ArgumentCaptor.forClass(Currencies.class);
        verify(currenciesRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("USD");
        assertThat(captor.getValue().getName()).isEqualTo("Dollar");
        assertThat(captor.getValue().getNbrbId()).isEqualTo(123L);
    }

    @Test
    void setDataLoadingTransactionNewExchangeRateTest() {

        NbrbRateDto nbrbRateDto = new NbrbRateDto("USD", 123L, "Dollar", LocalDate.now(), 1L, new BigDecimal(1));
        List<NbrbRateDto> nbrbRateDtoList = List.of(nbrbRateDto);

        Currencies currencies = new Currencies();
        currencies.setCode("USD");
        currencies.setName("Dollar");
        currencies.setNbrbId(123L);

        when(currenciesRepository.findByNbrbId(123L)).thenReturn(Optional.of(currencies));

        when(exchangeRateRepository.findByCurrencyAndRateDate(currencies, LocalDate.now()))
                .thenReturn(Optional.empty());

        dataLoadingTransaction.dataLoadingTransaction(nbrbRateDtoList);

        verify(currenciesRepository, never()).save(any(Currencies.class));

        ArgumentCaptor<ExchangeRates> captor = ArgumentCaptor.forClass(ExchangeRates.class);
        verify(exchangeRateRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrency().getCode()).isEqualTo("USD");
        assertThat(captor.getValue().getDate()).isEqualTo(LocalDate.now());
        assertThat(captor.getValue().getRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(captor.getValue().getScale()).isEqualTo(1);
    }

    @Test
    void setDataLoadingTransactionExsistsExchangeRatesTest() {
        NbrbRateDto nbrbRateDto = new NbrbRateDto("USD", 123L, "Dollar", LocalDate.now(), 1L, new BigDecimal(1));
        List<NbrbRateDto> nbrbRateDtoList = List.of(nbrbRateDto);

        Currencies currencies = new Currencies();
        currencies.setCode("USD");
        currencies.setName("Dollar");
        currencies.setNbrbId(123L);

        when(currenciesRepository.findByNbrbId(123L)).thenReturn(Optional.of(currencies));

        ExchangeRates exchangeRates = new ExchangeRates();
        exchangeRates.setId(123L);
        exchangeRates.setRate(BigDecimal.ONE);
        exchangeRates.setScale(1L);
        exchangeRates.setDate(LocalDate.now());
        exchangeRates.setCurrency(currencies);

        when(exchangeRateRepository.findByCurrencyAndRateDate(currencies, LocalDate.now()))
                .thenReturn(Optional.of(exchangeRates));

        dataLoadingTransaction.dataLoadingTransaction(nbrbRateDtoList);

        verify(currenciesRepository, never()).save(any(Currencies.class));

        ArgumentCaptor<ExchangeRates> captor = ArgumentCaptor.forClass(ExchangeRates.class);
        verify(exchangeRateRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(123);
    }

    @Test
    void setDataLoadingTransactionNullNbrbTest() {
        List<NbrbRateDto> nbrbRateDtoList = new ArrayList<>();

        dataLoadingTransaction.dataLoadingTransaction(nbrbRateDtoList);

        verify(exchangeRateRepository, never()).save(any(ExchangeRates.class));
        verify(currenciesRepository, never()).save(any(Currencies.class));
    }
}
