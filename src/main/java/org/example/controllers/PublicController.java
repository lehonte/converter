package org.example.controllers;

import lombok.RequiredArgsConstructor;
import org.example.dto.ExchangeRateResponseDto;
import org.example.services.ExchangeRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PublicController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/rate")
    public ExchangeRateResponseDto getCurrencyPair(String code, @RequestParam(required = false)LocalDate date) {
        return exchangeRateService.getCurrencyPair(code, date);
    }


    @GetMapping("/rates/all")
    public List<ExchangeRateResponseDto> getAllCurrencies(LocalDate date) {
        return exchangeRateService.getAllCurrencies(date);
    }


}
