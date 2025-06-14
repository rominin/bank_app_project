package ru.practicum.java.frontui.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.frontui.dto.ExchangeRateResponseDto;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeClient {

    @Value("${exchange-service.url}")
    private String exchangeServiceUrl;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Retry(name = "getRatesRetry", fallbackMethod = "fallbackGetRates")
    @CircuitBreaker(name = "getRatesCircuitBreaker", fallbackMethod = "fallbackGetRates")
    public List<ExchangeRateResponseDto> getRates(String base) {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("front-ui")
                .principal("front-client")
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        String token = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                exchangeServiceUrl + "/rates?base=" + base,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ExchangeRateResponseDto>>() {}
        ).getBody();
    }

    public List<ExchangeRateResponseDto> fallbackGetRates(String base, Throwable ex) {
        log.warn("Rates for base currency {} haven't been found: ", base, ex);
        return new ArrayList<>();
    }

}
