package ru.practicum.java.exchangeservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exchange_rates", schema = "exchange_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "from_currency")
    private String from;

    @Column(nullable = false, name = "to_currency")
    private String to;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private Instant timestamp;
}
