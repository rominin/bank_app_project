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
import ru.practicum.java.transferservice.dto.UpdateBalanceRequestDto;
import ru.practicum.java.transferservice.dto.UserAccountDto;

@Component
@RequiredArgsConstructor
public class AccountClient {

    @Value("${account-service.url}")
    private String accountServiceUrl;

    private final RestTemplate restTemplate;

    @Retry(name = "accountRetry", fallbackMethod = "fallbackGetAccount")
    @CircuitBreaker(name = "accountCircuitBreaker", fallbackMethod = "fallbackGetAccount")
    public UserAccountDto getAccount(Long accountId) {
        ResponseEntity<UserAccountDto> profileResponse = restTemplate.exchange(
                accountServiceUrl + "/" + accountId,
                HttpMethod.GET,
                null,
                UserAccountDto.class
        );

        UserAccountDto user = profileResponse.getBody();
        if (user == null) {
            throw new IllegalStateException("User profile could not be loaded");
        }

        System.out.println("Ответ от account-service: " + profileResponse.getBody());

        return user;
    }

    public UserAccountDto fallbackGetAccount(Long accountId, Throwable ex) {
        System.out.println("Fallback triggered, request for account: " + accountId + " " + ex.getMessage());
        return null;
    }

    @Retry(name = "updateRetry", fallbackMethod = "fallbackUpdate")
    @CircuitBreaker(name = "updateCircuitBreaker", fallbackMethod = "fallbackUpdate")
    public void updateBalance(Long accountId, UpdateBalanceRequestDto requestDto) {
        restTemplate.exchange(
                accountServiceUrl + "/" + accountId + "/balance",
                HttpMethod.PUT,
                new HttpEntity<>(requestDto),
                Void.class
        );
        System.out.println("Ответ от account-service: баланс обновили");
    }

    public void fallbackUpdate(Long accountId, UpdateBalanceRequestDto requestDto, Throwable ex) {
        System.out.println("Fallback triggered, request: " + requestDto + " for account: " + accountId + " " + ex.getMessage());
        throw new RuntimeException("Balance update failed", ex);
    }
}
