package ru.practicum.java.blockerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.java.blockerservice.dto.BlockCheckRequest;
import ru.practicum.java.blockerservice.dto.BlockCheckResponse;

import java.math.BigDecimal;

@Service
@Slf4j
public class BlockerService {

    private static final BigDecimal BLOCK_THRESHOLD = BigDecimal.valueOf(100_000);

    public BlockCheckResponse check(BlockCheckRequest request) {
        boolean blocked = request.getAmount().compareTo(BLOCK_THRESHOLD) > 0;

        if (blocked) {
            String reason = String.format("Operation blocked: %.2f > %.2f RUB", request.getAmount(), BLOCK_THRESHOLD);
            log.warn("BLOCKED {} for user {} - {}", request.getOperationType(), request.getUserId(), reason);
            return new BlockCheckResponse(true, reason);
        }

        log.info("ALLOWED {} for user {} - OK", request.getOperationType(), request.getUserId());
        return new BlockCheckResponse(false, "Operation allowed.");
    }

}
