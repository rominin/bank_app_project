package ru.practicum.java.frontui.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.java.frontui.dto.UserAccountDto;
import ru.practicum.java.frontui.dto.UserDto;
import ru.practicum.java.frontui.dto.UserUpdateDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountClient {

    @Value("${account-service.profile-url}")
    private String accountServiceProfileUrl;

    @Value("${account-service.account-url}")
    private String accountServiceAccountUrl;

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Retry(name = "userInfoRetry", fallbackMethod = "fallbackGetUserInfo")
    @CircuitBreaker(name = "userInfoCircuitBreaker", fallbackMethod = "fallbackGetUserInfo")
    public UserDto getUserInfo(HttpServletRequest request) {
        String token = extractJwtFromCookie(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                accountServiceProfileUrl,
                HttpMethod.GET,
                entity,
                UserDto.class
        ).getBody();
    }

    public UserDto fallbackGetUserInfo(HttpServletRequest request, Throwable ex) {
        log.warn("User info hasn't been retrieved: {} {}", request, ex.getMessage(), ex);
        return null;
    }

    @Retry(name = "accountsRetry", fallbackMethod = "fallbackGetAccounts")
    @CircuitBreaker(name = "accountsCircuitBreaker", fallbackMethod = "fallbackGetAccounts")
    public List<UserAccountDto> getAccounts(Long userId) {
        String token = getM2mToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(accountServiceAccountUrl)
                .queryParam("userId", userId)
                .build().toUriString();

        ResponseEntity<UserAccountDto[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserAccountDto[].class);
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public List<UserAccountDto> fallbackGetAccounts(Long userId, Throwable ex) {
        log.warn("Accounts for user {} haven't been retrieved: {}", userId, ex.getMessage(), ex);
        return Collections.emptyList();
    }

    @Retry(name = "deleteProfileRetry", fallbackMethod = "fallbackDeleteProfile")
    @CircuitBreaker(name = "deleteProfileCircuitBreaker", fallbackMethod = "fallbackDeleteProfile")
    public void deleteProfile(HttpServletRequest request) {
        String token = extractJwtFromCookie(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(accountServiceProfileUrl, HttpMethod.DELETE, entity, Void.class);
    }

    public void fallbackDeleteProfile(HttpServletRequest request, Throwable ex) {
        log.warn("Profile delete request {} failed: {}", request, ex.getMessage(), ex);
    }

    @Retry(name = "changePasswordRetry", fallbackMethod = "fallbackChangePassword")
    @CircuitBreaker(name = "deleteProfileCircuitBreaker", fallbackMethod = "fallbackChangePassword")
    public void changePassword(String newPassword, HttpServletRequest request) {
        String token = extractJwtFromCookie(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(newPassword, headers);
        restTemplate.exchange(accountServiceProfileUrl + "/password", HttpMethod.PUT, entity, Void.class);
    }

    public void fallbackChangePassword(String newPassword, HttpServletRequest request, Throwable ex) {
        log.warn("Password change request {} failed: {}", request, ex.getMessage(), ex);
    }

    @Retry(name = "updateProfileRetry", fallbackMethod = "fallbackUpdateProfile")
    @CircuitBreaker(name = "updateProfileCircuitBreaker", fallbackMethod = "fallbackUpdateProfile")
    public void updateProfile(UserUpdateDto updatedProfile, HttpServletRequest request) {
        String token = extractJwtFromCookie(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserUpdateDto> entity = new HttpEntity<>(updatedProfile, headers);
        restTemplate.exchange(accountServiceProfileUrl, HttpMethod.PUT, entity, Void.class);
    }

    public void fallbackUpdateProfile(UserUpdateDto updatedProfile, HttpServletRequest request, Throwable ex) {
        log.warn("Profile update request {} failed: {}", request, ex.getMessage(), ex);
    }

    @Retry(name = "deleteAccountRetry", fallbackMethod = "fallbackDeleteAccount")
    @CircuitBreaker(name = "deleteAccountCircuitBreaker", fallbackMethod = "fallbackDeleteAccount")
    public void deleteAccount(Long userId, String currency) {
        String token = getM2mToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(accountServiceAccountUrl)
                .queryParam("userId", userId)
                .queryParam("currency", currency)
                .toUriString();

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void fallbackDeleteAccount(Long userId, String currency, Throwable ex) {
        log.warn("Account delete request for userId {} and currency {} failed: {}", userId, currency, ex.getMessage(), ex);
    }

    @Retry(name = "addAccountRetry", fallbackMethod = "fallbackAddAccount")
    @CircuitBreaker(name = "addAccountCircuitBreaker", fallbackMethod = "fallbackAddAccount")
    public void addAccount(Long userId, String currency) {
        String token = getM2mToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(accountServiceAccountUrl)
                .queryParam("userId", userId)
                .queryParam("currency", currency)
                .toUriString();

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void fallbackAddAccount(Long userId, String currency, Throwable ex) {
        log.warn("Account add request for userId {} and currency {} failed: {}", userId, currency, ex.getMessage(), ex);
    }

    @Retry(name = "updateAccountRetry", fallbackMethod = "fallbackUpdateAccount")
    @CircuitBreaker(name = "updateAccountCircuitBreaker", fallbackMethod = "fallbackUpdateAccount")
    public void updateAccountCurrency(Long accountId, Long userId, String currency) {
        String token = getM2mToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder
                .fromHttpUrl(accountServiceAccountUrl)
                .queryParam("accountId", accountId)
                .queryParam("userId", userId)
                .queryParam("currency", currency)
                .toUriString();

        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public void fallbackUpdateAccount(Long accountId, Long userId, String currency, Throwable ex) {
        log.warn("Account update request for userId {} and currency {} failed: {}", userId, currency, ex.getMessage(), ex);
    }

    @Retry(name = "findAccountRetry", fallbackMethod = "fallbackFindAccount")
    @CircuitBreaker(name = "findAccountCircuitBreaker", fallbackMethod = "fallbackFindAccount")
    public List<UserAccountDto> findAccountsByUsername(String username) {
        String token = getM2mToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                accountServiceAccountUrl + "/byUsername/" + username,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<UserAccountDto>>() {}
        ).getBody();
    }

    public List<UserAccountDto> fallbackFindAccount(String username, Throwable ex) {
        log.warn("Account {} hasn't been found.", username, ex);
        return Collections.emptyList();
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if ("JWT".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new RuntimeException("JWT not found in cookie");
    }

    private String getM2mToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("front-ui")
                .principal("front-client")
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        String token = client.getAccessToken().getTokenValue();
        return token;
    }

}
