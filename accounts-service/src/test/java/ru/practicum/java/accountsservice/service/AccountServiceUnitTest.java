package ru.practicum.java.accountsservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.java.accountsservice.client.NotificationClient;
import ru.practicum.java.accountsservice.dto.NotificationRequestDto;
import ru.practicum.java.accountsservice.dto.UpdateBalanceRequest;
import ru.practicum.java.accountsservice.entity.Currency;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.entity.UserAccount;
import ru.practicum.java.accountsservice.repository.UserAccountRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AccountService.class)
public class AccountServiceUnitTest {

    @MockitoBean
    private UserAccountRepository userAccountRepository;
    @MockitoBean
    private NotificationClient notificationClient;
    @MockitoBean
    private ProfileService profileService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private AccountService accountService;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
    }

    @Test
    void testAddAccount_success() {
        when(profileService.getUserProfile(1L)).thenReturn(user);
        when(userAccountRepository.findByUserAndCurrency(user, Currency.USD)).thenReturn(Optional.empty());

        accountService.addAccount(1L, Currency.USD);

        verify(userAccountRepository).save(any(UserAccount.class));
        verify(notificationClient).notify(any(NotificationRequestDto.class));
    }

    @Test
    void testAddAccount_duplicate_shouldThrow() {
        when(profileService.getUserProfile(1L)).thenReturn(user);
        when(userAccountRepository.findByUserAndCurrency(user, Currency.USD)).thenReturn(Optional.of(new UserAccount()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.addAccount(1L, Currency.USD));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testDeleteAccount_success() {
        UserAccount account = new UserAccount();
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);

        when(profileService.getUserProfile(1L)).thenReturn(user);
        when(userAccountRepository.findByUserAndCurrency(user, Currency.RUB)).thenReturn(Optional.of(account));

        accountService.deleteAccountByCurrency(1L, Currency.RUB);

        verify(userAccountRepository).delete(account);
        verify(notificationClient).notify(any(NotificationRequestDto.class));
    }

    @Test
    void testDeleteAccount_withBalance_shouldThrow() {
        UserAccount account = new UserAccount();
        account.setBalance(BigDecimal.TEN);
        account.setUser(user);

        when(profileService.getUserProfile(1L)).thenReturn(user);
        when(userAccountRepository.findByUserAndCurrency(user, Currency.RUB)).thenReturn(Optional.of(account));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> accountService.deleteAccountByCurrency(1L, Currency.RUB));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testUpdateBalance_withdrawSuccess() {
        UserAccount account = new UserAccount();
        account.setId(10L);
        account.setBalance(BigDecimal.valueOf(100));

        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(account));

        UpdateBalanceRequest request = new UpdateBalanceRequest( BigDecimal.valueOf(40), "WITHDRAW");
        accountService.updateBalance(10L, request);

        assertEquals(BigDecimal.valueOf(60), account.getBalance());
        verify(userAccountRepository).save(account);
    }

    @Test
    void testUpdateBalance_withdrawInsufficient_shouldThrow() {
        UserAccount account = new UserAccount();
        account.setBalance(BigDecimal.valueOf(10));

        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(account));

        UpdateBalanceRequest request = new UpdateBalanceRequest(BigDecimal.valueOf(20),"WITHDRAW");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateBalance(10L, request));
    }

}
