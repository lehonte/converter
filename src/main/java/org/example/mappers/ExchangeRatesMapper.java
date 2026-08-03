package org.example.mappers;

import org.example.dto.ExchangeRateResponseDto;
import org.example.entities.ExchangeRates;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExchangeRatesMapper {

    @Mapping(target = "code", expression = "java(exchangeRates.getCurrency().getCode() + \"/BYN\")")
    ExchangeRateResponseDto toExchangeRateResponseDto(ExchangeRates exchangeRates);

    List<ExchangeRateResponseDto> toExchangeRateResponseDtoList(List<ExchangeRates> exchangeRatesList);
}
