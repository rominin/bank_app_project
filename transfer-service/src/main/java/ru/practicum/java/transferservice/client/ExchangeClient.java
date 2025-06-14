package ru.practicum.java.transferservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.transferservice.dto.ExchangeRateResponseDto;
import ru.practicum.java.transferservice.entity.Currency;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ExchangeClient {

    @Value("${exchange-service.url}")
    private String exchangeServiceUrl;

    private final RestTemplate restTemplate;

    @Retry(name = "exchangeRetry", fallbackMethod = "fallbackExchange")
    @CircuitBreaker(name = "exchangeCircuitBreaker", fallbackMethod = "fallbackExchange")
    public BigDecimal getExchangeRate(Currency from, Currency to) {
        String url = String.format("%s/rate?from=%s&to=%s", exchangeServiceUrl, from, to);

        ResponseEntity<ExchangeRateResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ExchangeRateResponseDto.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Exchange rate unavailable");
        }

        return response.getBody().getRate();
    }

    public BigDecimal fallbackExchange(Currency from, Currency to, Throwable ex) {
        System.out.println("From " + from + " to " + to + "request failed. Fallback triggered: " + ex.getMessage());
        return BigDecimal.ZERO;
    }

}
