package ru.practicum.java.transferservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.transferservice.client.AccountClient;
import ru.practicum.java.transferservice.client.BlockerClient;
import ru.practicum.java.transferservice.client.ExchangeClient;
import ru.practicum.java.transferservice.client.NotificationClient;
import ru.practicum.java.transferservice.dto.TransferRequestDto;
import ru.practicum.java.transferservice.dto.UserAccountDto;
import ru.practicum.java.transferservice.entity.Currency;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TransferService.class)
public class TransferServiceUnitTest {

    @MockitoBean
    private AccountClient accountClient;
    @MockitoBean
    private BlockerClient blockerClient;
    @MockitoBean
    private ExchangeClient exchangeClient;
    @MockitoBean
    private NotificationClient notificationClient;

    @Autowired
    private TransferService transferService;

    @Test
    void transfer_shouldExecuteTransfer_whenNotBlockedAndSameCurrency() {
        // given
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        TransferRequestDto request = TransferRequestDto.builder()
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .build();

        UserAccountDto from = UserAccountDto.builder()
                .accountId(fromId)
                .userId(10L)
                .username("Alice")
                .currency(Currency.USD)
                .build();

        UserAccountDto to = UserAccountDto.builder()
                .accountId(toId)
                .userId(11L)
                .username("Bob")
                .currency(Currency.USD)
                .build();

        when(accountClient.getAccount(fromId)).thenReturn(from);
        when(accountClient.getAccount(toId)).thenReturn(to);
        when(blockerClient.isBlocked(any())).thenReturn(false);

        // when
        transferService.transfer(request);

        // then
        verify(accountClient).updateBalance(eq(fromId), argThat(dto ->
                dto.getAmount().compareTo(amount) == 0 && dto.getOperationType().equals("WITHDRAW")
        ));

        verify(accountClient).updateBalance(eq(toId), argThat(dto ->
                dto.getAmount().compareTo(amount) == 0 && dto.getOperationType().equals("DEPOSIT")
        ));

        verify(notificationClient).notify(argThat(dto ->
                dto.getUserName().equals("Alice") &&
                        dto.getMessage().equals("WITHDRAW 100,00 USD") // <- с запятой!
        ));
    }

    @Test
    void transfer_shouldThrowException_whenBlocked() {
        // given
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = new BigDecimal("10000.00");

        TransferRequestDto request = TransferRequestDto.builder()
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .build();

        UserAccountDto from = UserAccountDto.builder()
                .accountId(fromId)
                .userId(10L)
                .username("Alice")
                .currency(Currency.USD)
                .build();

        UserAccountDto to = UserAccountDto.builder()
                .accountId(toId)
                .userId(11L)
                .username("Bob")
                .currency(Currency.USD)
                .build();

        when(accountClient.getAccount(fromId)).thenReturn(from);
        when(accountClient.getAccount(toId)).thenReturn(to);
        when(blockerClient.isBlocked(any())).thenReturn(true);

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                transferService.transfer(request)
        );
        assertThat(ex.getMessage()).contains("Operation blocked");

        verify(accountClient, never()).updateBalance(anyLong(), any());
        verify(notificationClient, never()).notify(any());
    }

    @Test
    void transfer_shouldConvertCurrency_whenDifferentCurrencies() {
        // given
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal rate = new BigDecimal("0.9");

        TransferRequestDto request = TransferRequestDto.builder()
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .build();

        UserAccountDto from = UserAccountDto.builder()
                .accountId(fromId)
                .userId(10L)
                .username("Alice")
                .currency(Currency.USD)
                .build();

        UserAccountDto to = UserAccountDto.builder()
                .accountId(toId)
                .userId(11L)
                .username("Bob")
                .currency(Currency.EUR)
                .build();

        when(accountClient.getAccount(fromId)).thenReturn(from);
        when(accountClient.getAccount(toId)).thenReturn(to);
        when(blockerClient.isBlocked(any())).thenReturn(false);
        when(exchangeClient.getExchangeRate(Currency.USD, Currency.EUR)).thenReturn(rate);

        // when
        transferService.transfer(request);

        // then
        verify(accountClient).updateBalance(eq(toId), argThat(dto ->
                dto.getAmount().compareTo(amount.multiply(rate)) == 0 &&
                        dto.getOperationType().equals("DEPOSIT")
        ));
    }

}
