package ru.practicum.java.blockerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockCheckResponse {
    private boolean blocked;
    private String reason;
}
