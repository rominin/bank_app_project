package ru.practicum.java.exchangeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ExchangeRateResponseDto {
    private BigDecimal rate;
}
