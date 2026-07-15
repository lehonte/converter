package org.example.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
public class Currencies {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "nbrb_id", nullable = false, unique = true)
  private long nbrbId;

  @Column(name = "name", nullable = false)
  private String name;



}
