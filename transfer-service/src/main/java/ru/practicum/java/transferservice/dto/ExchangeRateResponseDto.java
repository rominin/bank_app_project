package ru.practicum.java.transferservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateResponseDto {
    private BigDecimal rate;
}
