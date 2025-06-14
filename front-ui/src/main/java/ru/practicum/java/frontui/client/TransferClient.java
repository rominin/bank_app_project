package ru.practicum.java.frontui.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.frontui.dto.TransferRequestDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferClient {

    @Value("${transfer-service.url}")
    private String transferServiceUrl;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Retry(name = "transferRetry", fallbackMethod = "fallbackTransfer")
    @CircuitBreaker(name = "transferCircuitBreaker", fallbackMethod = "fallbackTransfer")
    public void transfer(TransferRequestDto dto) {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("front-ui")
                .principal("front-client")
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        String token = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<TransferRequestDto> entity = new HttpEntity<>(dto, headers);


        restTemplate.exchange(transferServiceUrl, HttpMethod.POST, entity, Void.class);
    }

    public void fallbackTransfer(TransferRequestDto dto, Throwable ex) {
        log.warn(ex.getMessage(), ex);
    }

}
