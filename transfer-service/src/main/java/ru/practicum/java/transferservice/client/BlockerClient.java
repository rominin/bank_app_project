package ru.practicum.java.transferservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.transferservice.dto.BlockCheckRequestDto;
import ru.practicum.java.transferservice.dto.BlockCheckResponseDto;

@Component
@RequiredArgsConstructor
public class BlockerClient {

    @Value("${blocker-service.url}")
    private String blockerServiceUrl;

    private final RestTemplate restTemplate;

    @Retry(name = "checkBlockRetry", fallbackMethod = "fallbackCheckBlock")
    @CircuitBreaker(name = "checkBlockCircuitBreaker", fallbackMethod = "fallbackCheckBlock")
    public boolean isBlocked(BlockCheckRequestDto checkRequest) {
        HttpEntity<BlockCheckRequestDto> entity = new HttpEntity<>(checkRequest);

        ResponseEntity<BlockCheckResponseDto> response = restTemplate.exchange(blockerServiceUrl, HttpMethod.POST, entity, BlockCheckResponseDto.class);

        return response.getBody() != null && response.getBody().isBlocked();
    }

    public boolean fallbackCheckBlock(BlockCheckRequestDto checkRequest, Throwable ex) {
        System.out.println("Fallback triggered for check request: " + checkRequest + " " + ex.getMessage());
        return true;
    }

}
