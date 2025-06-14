package ru.practicum.java.accountsservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.accountsservice.dto.NotificationRequestDto;

@Component
public class NotificationClient {

    @Value("${notification-service.url}")
    private String notificationServiceUrl;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    public NotificationClient(OAuth2AuthorizedClientManager authorizedClientManager, RestTemplate restTemplate) {
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = restTemplate;
    }

    @Retry(name = "notifyRetry", fallbackMethod = "fallbackNotify")
    @CircuitBreaker(name = "notifyCircuitBreaker", fallbackMethod = "fallbackNotify")
    public void notify(NotificationRequestDto notification) {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("account-service")
                .principal("notification-client")
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        String token = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<NotificationRequestDto> entity = new HttpEntity<>(notification, headers);

        ResponseEntity<String> response =  restTemplate.postForEntity(notificationServiceUrl, entity, String.class);

        System.out.println("Ответ от notification-service: " + response.getBody());
    }

    public void fallbackNotify(NotificationRequestDto notification, Throwable ex) {
        System.out.println("Fallback triggered: " + ex.getMessage() + ".  This hasn't been sent successfully: " + notification);
    }

}
