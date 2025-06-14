package ru.practicum.java.accountsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountDto {
    private Long accountId;
    private Long userId;
    private String username;
    private String currency;
    private BigDecimal balance;
}
