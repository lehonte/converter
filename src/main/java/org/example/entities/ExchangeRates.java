package org.example.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
  private Currencies currencyId;

  @Column(name = "rate", nullable = false)
  private String rate;

  @Column(name = "scale", nullable = false)
  private long scale;

  @Column(name = "rate_date", nullable = false)
  private java.sql.Date rateDate;

}
