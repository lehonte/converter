package org.example.controllers;

import lombok.RequiredArgsConstructor;
import org.example.services.ExchangeRateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping("/rates/upload")
    public void dataLoading() {
        exchangeRateService.dataLoading();
    }

}
