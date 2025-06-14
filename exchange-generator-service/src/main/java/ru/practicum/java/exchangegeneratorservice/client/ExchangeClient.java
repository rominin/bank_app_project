package ru.practicum.java.exchangegeneratorservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.exchangegeneratorservice.dto.CurrencyRateDto;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeClient {

    @Value("${exchange-service.url}")
    private String exchangeServiceUrl;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Retry(name = "sendRateToExchangeServiceRetry", fallbackMethod = "fallbackSend")
    @CircuitBreaker(name = "sendRateToExchangeServiceBreaker", fallbackMethod = "fallbackSend")
    public void send(CurrencyRateDto rate) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<CurrencyRateDto> entity = new HttpEntity<>(rate, headers);

        restTemplate.postForEntity(exchangeServiceUrl, entity, Void.class);
    }

    private String getAccessToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("exchange-generator-service")
                .principal("exchange-generator-service-to-exchange-service-client")
                .build();

        return Optional.ofNullable(authorizedClientManager.authorize(request))
                .map(client -> client.getAccessToken().getTokenValue())
                .orElseThrow(() -> new IllegalStateException("Didn't manage to retrieve access token"));
    }

    public void fallbackSend(CurrencyRateDto rate, Throwable ex) {
        log.warn("Failed to send exchange rate {} → {}: {}, fallback activated", rate.getFrom(), rate.getTo(), ex.getMessage());
    }

}
