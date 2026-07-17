package org.example.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.dto.ExchangeRateResponseDto;
import org.example.services.ExchangeRateService;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class PublicController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/rate")
    public ExchangeRateResponseDto getCurrencyPair(@NotBlank(message = "Код валюты не должен быть пустым")
                                                       @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                       @RequestParam String code,
                                                   @RequestParam(required = false)LocalDate date) {
        return exchangeRateService.getCurrencyPair(code, date);
    }


    @GetMapping("/rates/all")
    public List<ExchangeRateResponseDto> getAllCurrencies(@RequestParam(required = false) LocalDate date) {
        return exchangeRateService.getAllCurrencies(date);
    }

    @GetMapping("/rates/history")
    public List<ExchangeRateResponseDto> getAllCurrenciesForTime(@NotBlank(message = "Код валюты не должен быть пустым")
                                                                     @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                                     @RequestParam String code,
                                                                 @RequestParam("from") LocalDate fromDate,
                                                                 @RequestParam("to") LocalDate toDate) {
        return exchangeRateService.getAllCurrenciesForTime(code, fromDate, toDate);
    }

    @GetMapping("/convert")
    public ExchangeRateResponseDto getExchangeRateBetweenTwoCurrencies(@NotBlank(message = "Код валюты не должен быть пустым")
                                                                           @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                                           @RequestParam("first") String firstCode,
                                                                       @NotBlank(message = "Код валюты не должен быть пустым")
                                                                       @Pattern(regexp = "^[A-Z]{3}$", message = "Неверный тип данных")
                                                                       @RequestParam("second") String secondCode,
                                                                       @RequestParam(required = false) LocalDate date
                                                                       ) {
        return exchangeRateService.getExchangeRateBetweenTwoCurrencies(firstCode, secondCode, date);
    }
}
