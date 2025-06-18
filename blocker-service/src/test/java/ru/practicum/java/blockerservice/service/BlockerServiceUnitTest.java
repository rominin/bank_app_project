package ru.practicum.java.blockerservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.java.blockerservice.dto.BlockCheckRequest;
import ru.practicum.java.blockerservice.dto.BlockCheckResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BlockerService.class)
public class BlockerServiceUnitTest {

    @Autowired
    private BlockerService blockerService;

    @Test
    void testCheck_shouldAllow_whenAmountBelowThreshold() {
        BlockCheckRequest request = BlockCheckRequest.builder()
                .userId("123")
                .operationType("WITHDRAW")
                .amount(BigDecimal.valueOf(99_999))
                .build();

        BlockCheckResponse response = blockerService.check(request);

        assertFalse(response.isBlocked());
        assertEquals("Operation allowed.", response.getReason());
    }

    @Test
    void testCheck_shouldBlock_whenAmountAboveThreshold() {
        BlockCheckRequest request = BlockCheckRequest.builder()
                .userId("123")
                .operationType("WITHDRAW")
                .amount(BigDecimal.valueOf(100_001))
                .build();

        BlockCheckResponse response = blockerService.check(request);

        assertTrue(response.isBlocked());
        assertTrue(response.getReason().contains("Operation blocked"));
    }

    @Test
    void testCheck_shouldNotBlock_whenAmountEqualsThreshold() {
        BlockCheckRequest request = BlockCheckRequest.builder()
                .userId("123")
                .operationType("DEPOSIT")
                .amount(BigDecimal.valueOf(100_000))
                .build();

        BlockCheckResponse response = blockerService.check(request);

        assertFalse(response.isBlocked());
        assertEquals("Operation allowed.", response.getReason());
    }

}
