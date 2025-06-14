package ru.practicum.java.transferservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.transferservice.dto.NotificationRequestDto;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    @Value("${notification-service.url}")
    private String notificationServiceUrl;

    private final RestTemplate restTemplate;

    @Retry(name = "notifyRetry", fallbackMethod = "fallbackNotify")
    @CircuitBreaker(name = "notifyCircuitBreaker", fallbackMethod = "fallbackNotify")
    public void notify(NotificationRequestDto notification) {
        HttpEntity<NotificationRequestDto> entity = new HttpEntity<>(notification);

        ResponseEntity<String> response = restTemplate.postForEntity(notificationServiceUrl, entity, String.class);

        System.out.println("Ответ от notification-service: " + response.getBody());
    }

    public void fallbackNotify(NotificationRequestDto notification, Throwable ex) {
        System.out.println("Notification " + notification + " hasn't been sent. Fallback triggered: " + ex.getMessage());
    }

}
