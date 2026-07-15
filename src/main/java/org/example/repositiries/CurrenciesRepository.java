package org.example.repositiries;

import org.example.entities.Currencies;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrenciesRepository extends JpaRepository<Currencies, Long> {
    Currencies findByNbrbId(Long nbrbId);
}
