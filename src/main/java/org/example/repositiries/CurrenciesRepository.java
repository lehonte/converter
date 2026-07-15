package org.example.repositiries;

import org.example.entities.Currencies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrenciesRepository extends JpaRepository<Currencies, Long> {
    Optional<Currencies> findByNbrbId(Long nbrbId);
    Optional<Currencies> findByCode(String code);
}
