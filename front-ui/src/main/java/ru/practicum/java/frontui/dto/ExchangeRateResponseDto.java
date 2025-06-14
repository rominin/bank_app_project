package ru.practicum.java.frontui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateResponseDto {
    private String from;
    private String to;
    private BigDecimal rate;
}
