package org.example.repositories;

import org.example.entities.Currencies;
import org.example.entities.ExchangeRates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ExchangeRateRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    private Currencies currenciesInFakeDB;

    @BeforeEach
    void setUp() {
        Currencies currencies = new Currencies();
        currencies.setCode("USD");
        currencies.setName("Dollar");
        currencies.setNbrbId(123L);
        currenciesInFakeDB = entityManager.persist(currencies);

        ExchangeRates firstExchangeRates = new ExchangeRates();
        firstExchangeRates.setCurrency(currencies);
        firstExchangeRates.setDate(LocalDate.now());
        firstExchangeRates.setScale(1L);
        firstExchangeRates.setRate(new BigDecimal("3.24"));
        entityManager.persist(firstExchangeRates);

        ExchangeRates secondExchangeRates = new ExchangeRates();
        secondExchangeRates.setCurrency(currencies);
        secondExchangeRates.setDate(LocalDate.now().minusDays(1));
        secondExchangeRates.setScale(1L);
        secondExchangeRates.setRate(new BigDecimal("3"));
        entityManager.persist(secondExchangeRates);

        ExchangeRates thirdExchangeRates = new ExchangeRates();
        thirdExchangeRates.setCurrency(currencies);
        thirdExchangeRates.setDate(LocalDate.now().minusDays(2));
        thirdExchangeRates.setScale(1L);
        thirdExchangeRates.setRate(new BigDecimal("3.5"));
        entityManager.persist(thirdExchangeRates);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByCurrencyAndRateDateTest() {

        Optional<ExchangeRates> exchangeRatesFound = exchangeRateRepository
                .findByCurrencyAndRateDate(currenciesInFakeDB, LocalDate.now());

        assertThat(exchangeRatesFound).isPresent();
        assertThat(exchangeRatesFound.get().getRate()).isEqualByComparingTo("3.24");
        assertThat(exchangeRatesFound.get().getCurrency().getCode()).isEqualTo("USD");
    }

    @Test
    void findByCurrencyAndRateDateBetween() {

        List<ExchangeRates> foundExchangeRates = exchangeRateRepository
                .findByCurrencyAndRateDateBetween(currenciesInFakeDB, LocalDate.now().minusDays(1), LocalDate.now());

        assertThat(foundExchangeRates).isNotEmpty();
        assertThat(foundExchangeRates.size()).isEqualTo(2);
        assertThat(foundExchangeRates.get(0).getCurrency().getCode()).isEqualTo("USD");
        assertThat(foundExchangeRates)
                .extracting(ExchangeRates::getRate)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(new BigDecimal("3.24"), new BigDecimal("3"));
    }

    @Test
    void findByRateDateTest() {

        List<ExchangeRates> foundExchangeRates = exchangeRateRepository
                .findByRateDate(LocalDate.now().minusDays(1));

        assertThat(foundExchangeRates).isNotEmpty();
        assertThat(foundExchangeRates.size()).isEqualTo(1);
        assertThat(foundExchangeRates.get(0).getRate()).isEqualByComparingTo("3");
        assertThat(foundExchangeRates.get(0).getCurrency().getCode()).isEqualTo("USD");
    }

    @Test
    void tryToSaveSameRatesTest() {

        ExchangeRates exchangeRates = new ExchangeRates();
        exchangeRates.setCurrency(currenciesInFakeDB);
        exchangeRates.setDate(LocalDate.now());
        exchangeRates.setScale(1L);
        exchangeRates.setRate(new BigDecimal("3.24"));

        assertThatThrownBy(() -> exchangeRateRepository.save(exchangeRates))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}