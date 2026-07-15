package org.example.dto;

import lombok.Builder;

@Builder
public record NbrbRateDto(String code, int nbrbId,  String name) {
}
