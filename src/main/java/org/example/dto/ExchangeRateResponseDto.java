package org.example.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ExchangeRateResponseDto(String code, Long rate, LocalDate date) {
}
