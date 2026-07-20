package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Дто ответа пользователю с результатами курсов")
@Builder
public record ExchangeRateResponseDto(
        @Schema(description = "Кол-во валюты от", example = "1")
        Long scale,

        @Schema(description = "Код валюты", example = "RUB")
        String code,

        @Schema(description = "Курс к валюте", example = "2.43")
        BigDecimal rate,

        @Schema(description = "Дата актуальности курса", example = "2026-07-17")
        LocalDate date) {
}
