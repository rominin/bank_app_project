package ru.practicum.java.cashservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.cashservice.dto.UpdateBalanceRequestDto;
import ru.practicum.java.cashservice.dto.UserAccountDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountClient {

    @Value("${account-service.url}")
    private String accountServiceUrl;

    private final RestTemplate restTemplate;

    @Retry(name = "accountRetry", fallbackMethod = "fallbackGetAccount")
    @CircuitBreaker(name = "accountCircuitBreaker", fallbackMethod = "fallbackGetAccount")
    public UserAccountDto getAccount(Long accountId) {
        ResponseEntity<UserAccountDto> response = restTemplate.exchange(
                accountServiceUrl + "/" + accountId,
                HttpMethod.GET,
                null,
                UserAccountDto.class
        );

        UserAccountDto user = response.getBody();
        if (user == null) {
            throw new IllegalStateException("User profile could not be loaded");
        }

        log.info("Получен аккаунт {} с балансом {}", user.getAccountId(), user.getBalance());
        return user;
    }

    public UserAccountDto fallbackGetAccount(Long accountId, Throwable ex) {
        log.error("Fallback: не удалось получить аккаунт {} — {}", accountId, ex.getMessage());
        return null;
    }

    @Retry(name = "updateRetry", fallbackMethod = "fallbackUpdate")
    @CircuitBreaker(name = "updateCircuitBreaker", fallbackMethod = "fallbackUpdate")
    public void updateBalance(Long accountId, UpdateBalanceRequestDto dto) {
        restTemplate.exchange(
                accountServiceUrl + "/" + accountId + "/balance",
                HttpMethod.PUT,
                new HttpEntity<>(dto),
                Void.class
        );
        log.info("Баланс аккаунта {} обновлён: {}", accountId, dto);
    }

    public void fallbackUpdate(Long accountId, UpdateBalanceRequestDto dto, Throwable ex) {
        log.error("Fallback: не удалось обновить баланс аккаунта {} — {}", accountId, ex.getMessage());
    }

}
