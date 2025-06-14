package ru.practicum.java.exchangegeneratorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyRateDto {
    private String from;
    private String to;
    private BigDecimal rate;
    private Instant timestamp;
}
