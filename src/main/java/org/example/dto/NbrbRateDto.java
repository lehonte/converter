package org.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record NbrbRateDto(
        @JsonProperty("Cur_Abbreviation")
        String code,

        @JsonProperty("Cur_ID")
        Long nbrbId,

        @JsonProperty("Cur_Name")
        String name,

        @JsonProperty("Date")
        LocalDate date,

        @JsonProperty("Cur_Scale")
        Long scale,

        @JsonProperty("Cur_OfficialRate")
        BigDecimal rate) {
}