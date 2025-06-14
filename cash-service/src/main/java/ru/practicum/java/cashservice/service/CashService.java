package ru.practicum.java.cashservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.java.cashservice.client.AccountClient;
import ru.practicum.java.cashservice.client.BlockerClient;
import ru.practicum.java.cashservice.client.NotificationClient;
import ru.practicum.java.cashservice.dto.*;

@Service
@RequiredArgsConstructor
public class CashService {

    private final AccountClient accountClient;
    private final BlockerClient blockerClient;
    private final NotificationClient notificationClient;

    public void process(OperationRequestDto dto) {
        UserAccountDto account = accountClient.getAccount(dto.getAccountId());

        boolean blocked = blockerClient.isBlocked(BlockCheckRequestDto.builder()
                .userId(account.getUserId().toString())
                .operationType(dto.getOperationType())
                .amount(dto.getAmount())
                .build());

        if (blocked) {
            throw new RuntimeException("Operation blocked due to suspicious amount");
        }

        if ("WITHDRAW".equalsIgnoreCase(dto.getOperationType())
                && dto.getAmount().compareTo(account.getBalance()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdraw amount exceeds balance");
        }

        accountClient.updateBalance(account.getAccountId(), UpdateBalanceRequestDto.builder()
                .amount(dto.getAmount())
                .operationType(dto.getOperationType())
                .build());

        notificationClient.notify(NotificationRequestDto.builder()
                .userName(account.getUsername())
                .message(String.format("%s %.2f %s",
                        dto.getOperationType(), dto.getAmount(), account.getCurrency()))
                .build());

    }

}
