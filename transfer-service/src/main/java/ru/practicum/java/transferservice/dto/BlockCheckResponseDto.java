package ru.practicum.java.transferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockCheckResponseDto {
    private boolean blocked;
    private String reason;
}
