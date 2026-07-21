package org.example.connectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.NbrbRateDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NbrbConnector {

    private final NbrbConnectorRetryable nbrbConnectorRetryable;

    @CircuitBreaker(name = "nbrbApi", fallbackMethod = "fallbackRates")
    public List<NbrbRateDto> getNbrbRates(LocalDate date) {
        return nbrbConnectorRetryable.getNbrbRatesRetryable(date);
    }

    @SuppressWarnings("unused")
    private List<NbrbRateDto> fallbackRates(LocalDate date, Exception ex) {
        log.warn("Проблема соединения с внешним сервером");
        return List.of();
    }
}
