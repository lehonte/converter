package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.connectors.NbrbConnector;
import org.example.dto.NbrbRateDto;
import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.example.repositiries.CurrenciesRepository;
import org.example.repositiries.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final NbrbConnector nbrbConnector;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrenciesRepository currenciesRepository;

    public void dataLoading() {
        nbrbConnector.getNbrbRates(LocalDate.now())
                .forEach(nbrbRateDto -> {
                    Currencies currencies = currenciesRepository
                            .findByNbrbId(nbrbRateDto.nbrbId());

                    if (currencies == null) {
                        currencies = new Currencies();
                        currencies.setCode(nbrbRateDto.code());
                        currencies.setName(nbrbRateDto.name());
                        currencies.setNbrbId(nbrbRateDto.nbrbId());
                        currencies = currenciesRepository.save(currencies);
                    }

                    ExchangeRates exchangeRates = exchangeRateRepository
                            .findByCurrencyAndRateDate(currencies, nbrbRateDto.date());

                    if (exchangeRates == null) {
                        exchangeRates = new ExchangeRates();
                        exchangeRates.setCurrency(currencies);
                    }

                    exchangeRates.setRate(nbrbRateDto.rate());
                    exchangeRates.setRateDate(nbrbRateDto.date());
                    exchangeRates.setScale(nbrbRateDto.scale());
                    exchangeRateRepository.save(exchangeRates);
                });

    }
}
