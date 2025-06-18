package ru.practicum.java.accountsservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@TestConfiguration
public class NoAuthNotificationClientTestConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("test")
                .tokenUri("http://fake-token-uri")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId("fake-client")
                .clientSecret("fake-secret")
                .build();

        return request -> new OAuth2AuthorizedClient(
                registration,
                "notification-client",
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        "fake-token-value",
                        Instant.now(),
                        Instant.now().plusSeconds(3600)
                )
        );
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
