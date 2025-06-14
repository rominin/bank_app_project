package ru.practicum.java.cashservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockCheckRequestDto {
    private String operationType;
    private String userId;
    private BigDecimal amount;
}
