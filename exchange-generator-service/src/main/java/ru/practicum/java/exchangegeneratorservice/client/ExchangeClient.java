package ru.practicum.java.exchangegeneratorservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeClient {

    @Value("${exchange-service.url}")
    private String exchangeServiceUrl;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Retry(name = "sendRateToExchangeServiceRetry", fallbackMethod = "fallbackPing")
    @CircuitBreaker(name = "sendRateToExchangeServiceBreaker", fallbackMethod = "fallbackPing")
    public void pingExchangeService() {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        restTemplate.exchange(exchangeServiceUrl, HttpMethod.GET, new HttpEntity<Void>(headers), Void.class);
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

    public void fallbackPing(Throwable ex) {
        log.warn("Some issues occurred + ", ex.getMessage());
    }

}
