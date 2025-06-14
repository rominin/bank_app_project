package ru.practicum.java.blockerservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockCheckRequest {

    @NotBlank(message = "operationType is required")
    private String operationType;

    @NotBlank(message = "userId is required")
    private String userId;

    @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
    private BigDecimal amount;
}
