package org.example.controllers;

import org.example.dto.ExchangeRateResponseDto;
import org.example.exceptions.CurrencyNotFoundException;
import org.example.services.ExchangeRateService;
import org.example.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Test
    void getCurrencyPairValidRequestTest() throws Exception {
        when(exchangeRateService.getCurrencyPair(eq("USD"), any()))
                .thenReturn(new ExchangeRateResponseDto(1L, "USD/BYN", BigDecimal.valueOf(3.24), LocalDate.now()));

        mockMvc.perform(get("/api/v1/rate").param("code", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD/BYN"));
    }

    @ParameterizedTest
    @CsvSource({
            "usd, Неверный тип данных",
            "us, Неверный тип данных",
            "'', Код валюты не должен быть пустым"
    })
    void getCurrencyPairInvalidRequestTest(String code, String message) throws Exception {
        mockMvc.perform(get("/api/v1/rate").param("code", code))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString(message)));
    }

    @Test
    void getCurrencyPairCurrencyNotFoundTest() throws Exception {
        when(exchangeRateService.getCurrencyPair(eq("XCV"), any()))
                .thenThrow(CurrencyNotFoundException.class);

        mockMvc.perform(get("/api/v1/rate").param("code", "XCV"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void getExchangeRateBetweenTwoCurrenciesValidTest() throws Exception {
        when(exchangeRateService.getExchangeRateBetweenTwoCurrencies(eq("USD"), eq("EUR"), any()))
                .thenReturn(new ExchangeRateResponseDto(1L, "USD/EUR", BigDecimal.valueOf(0.981818), LocalDate.now()));

        mockMvc.perform(get("/api/v1/convert")
                .param("first", "USD")
                .param("second", "EUR")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD/EUR"));
    }

    @Test
    void getExchangeRateBetweenTwoCurrenciesInvalidTest() throws Exception {
        mockMvc.perform(get("/api/v1/convert")
                .param("first", "fgfgsgsg")
                .param("second", "EUR")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Неверный тип данных"));
    }

    @Test
    void getAllCurrenciesForTimeNoDatesInParametersTest() throws Exception {
        mockMvc.perform(get("/api/v1//rates/history")
                .param("code", "USD"))
                .andExpect(status().isBadRequest());
    }
}
