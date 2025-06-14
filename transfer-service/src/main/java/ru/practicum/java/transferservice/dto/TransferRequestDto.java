package ru.practicum.java.transferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDto {
    private Long fromAccountId;

    private Long toAccountId;

    private BigDecimal amount;
}
