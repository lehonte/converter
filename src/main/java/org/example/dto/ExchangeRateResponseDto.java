package org.example.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ExchangeRateResponseDto(String code, BigDecimal rate, LocalDate date) {
}
