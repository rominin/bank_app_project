package ru.practicum.java.transferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.java.transferservice.entity.Currency;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountDto {
    private Long accountId;
    private Long userId;
    private String username;
    private Currency currency;
    private BigDecimal balance;
}
