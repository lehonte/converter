package org.example.repositiries;

import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRates, Long> {
    Optional<ExchangeRates> findByCurrencyAndRateDate(Currencies currency, LocalDate rateDate);
}
