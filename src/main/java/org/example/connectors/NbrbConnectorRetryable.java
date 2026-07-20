package org.example.connectors;

import lombok.RequiredArgsConstructor;
import org.example.dto.NbrbRateDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NbrbConnectorRetryable {

    private final RestClient restClient;

    @Retryable(retryFor = {RestClientException.class}, maxAttempts = 5, backoff = @Backoff(delay = 5000))
    public List<NbrbRateDto> getNbrbRatesRetryable(LocalDate date) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/exrates/rates")
                        .queryParam("periodicity", 0)
                        .queryParamIfPresent("ondate", Optional.ofNullable(date))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
