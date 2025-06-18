package ru.practicum.java.cashservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.cashservice.client.AccountClient;
import ru.practicum.java.cashservice.client.BlockerClient;
import ru.practicum.java.cashservice.client.NotificationClient;
import ru.practicum.java.cashservice.dto.NotificationRequestDto;
import ru.practicum.java.cashservice.dto.OperationRequestDto;
import ru.practicum.java.cashservice.dto.UpdateBalanceRequestDto;
import ru.practicum.java.cashservice.dto.UserAccountDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CashService.class)
public class CashServiceUnitTest {

    @MockitoBean
    private AccountClient accountClient;
    @MockitoBean
    private BlockerClient blockerClient;
    @MockitoBean
    private NotificationClient notificationClient;

    @Autowired
    private CashService cashService;

    @Test
    void testProcess_deposit_success() {
        OperationRequestDto request = OperationRequestDto.builder()
                .accountId(1L)
                .amount(BigDecimal.valueOf(500))
                .operationType("DEPOSIT")
                .build();

        UserAccountDto account = UserAccountDto.builder()
                .accountId(1L)
                .userId(42L)
                .username("john")
                .currency("RUB")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(accountClient.getAccount(1L)).thenReturn(account);
        when(blockerClient.isBlocked(any())).thenReturn(false);

        cashService.process(request);

        verify(accountClient).updateBalance(eq(1L), any(UpdateBalanceRequestDto.class));
        verify(notificationClient).notify(any(NotificationRequestDto.class));
    }

    @Test
    void testProcess_withdraw_success() {
        OperationRequestDto request = OperationRequestDto.builder()
                .accountId(2L)
                .amount(BigDecimal.valueOf(300))
                .operationType("WITHDRAW")
                .build();

        UserAccountDto account = UserAccountDto.builder()
                .accountId(2L)
                .userId(77L)
                .username("anna")
                .currency("USD")
                .balance(BigDecimal.valueOf(500))
                .build();

        when(accountClient.getAccount(2L)).thenReturn(account);
        when(blockerClient.isBlocked(any())).thenReturn(false);

        cashService.process(request);

        verify(accountClient).updateBalance(eq(2L), any(UpdateBalanceRequestDto.class));
        verify(notificationClient).notify(any(NotificationRequestDto.class));
    }

    @Test
    void testProcess_blocked_shouldThrowException() {
        OperationRequestDto request = OperationRequestDto.builder()
                .accountId(3L)
                .amount(BigDecimal.valueOf(99999))
                .operationType("WITHDRAW")
                .build();

        UserAccountDto account = UserAccountDto.builder()
                .accountId(3L)
                .userId(55L)
                .username("blocked_user")
                .currency("CNY")
                .balance(BigDecimal.valueOf(99999))
                .build();

        when(accountClient.getAccount(3L)).thenReturn(account);
        when(blockerClient.isBlocked(any())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cashService.process(request));

        assertEquals("Operation blocked due to suspicious amount", exception.getMessage());
        verify(accountClient, never()).updateBalance(anyLong(), any());
        verify(notificationClient, never()).notify(any());
    }

    @Test
    void testProcess_withdrawMoreThanBalance_shouldThrow() {
        OperationRequestDto request = OperationRequestDto.builder()
                .accountId(4L)
                .amount(BigDecimal.valueOf(1000))
                .operationType("WITHDRAW")
                .build();

        UserAccountDto account = UserAccountDto.builder()
                .accountId(4L)
                .userId(10L)
                .username("max")
                .currency("EUR")
                .balance(BigDecimal.valueOf(200))
                .build();

        when(accountClient.getAccount(4L)).thenReturn(account);
        when(blockerClient.isBlocked(any())).thenReturn(false);

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> cashService.process(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getBody().getStatus());
        assertEquals("Withdraw amount exceeds balance", exception.getReason());
        verify(accountClient, never()).updateBalance(anyLong(), any());
        verify(notificationClient, never()).notify(any());
    }

}
