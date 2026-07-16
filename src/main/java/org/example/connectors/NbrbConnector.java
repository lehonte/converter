package org.example.connectors;

import lombok.RequiredArgsConstructor;
import org.example.dto.NbrbRateDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NbrbConnector {

    private final RestClient restClient;

    public List<NbrbRateDto> getNbrbRates(LocalDate date) {
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
