package org.example.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
public class ExchangeRates {

  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "currency_id")
  private Currencies currency;

  @Column(name = "rate", nullable = false)
  private BigDecimal rate;

  @Column(name = "scale", nullable = false)
  private long scale;

  @Column(name = "rate_date", nullable = false)
  private LocalDate rateDate;

}
