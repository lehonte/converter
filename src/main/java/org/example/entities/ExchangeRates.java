package org.example.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
public class ExchangeRates {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne
  @JoinColumn(name = "currency_id")
  private Currencies currency;

  @Column(name = "rate", nullable = false)
  private Long rate;

  @Column(name = "scale", nullable = false)
  private long scale;

  @Column(name = "rate_date", nullable = false)
  private LocalDate rateDate;

}
