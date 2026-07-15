package org.example.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record NbrbRateDto(String code, Long nbrbId,  String name, LocalDate date, Long scale, Long rate) {
}