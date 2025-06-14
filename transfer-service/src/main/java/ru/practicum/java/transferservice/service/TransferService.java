package ru.practicum.java.transferservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.java.transferservice.client.AccountClient;
import ru.practicum.java.transferservice.client.BlockerClient;
import ru.practicum.java.transferservice.client.ExchangeClient;
import ru.practicum.java.transferservice.client.NotificationClient;
import ru.practicum.java.transferservice.dto.*;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountClient accountClient;
    private final BlockerClient blockerClient;
    private final ExchangeClient exchangeClient;
    private final NotificationClient notificationClient;

    public void transfer(TransferRequestDto request) {
        UserAccountDto from = accountClient.getAccount(request.getFromAccountId());
        UserAccountDto to = accountClient.getAccount(request.getToAccountId());

        boolean blocked = blockerClient.isBlocked(BlockCheckRequestDto.builder()
                .userId(from.getUserId().toString())
                .operationType("WITHDRAW")
                .amount(request.getAmount())
                .build());

        if (blocked) {
            throw new RuntimeException("Operation blocked due to suspicious amount");
        }

        BigDecimal amountToWithdraw = request.getAmount();
        BigDecimal amountToDeposit;

        if (from.getCurrency().equals(to.getCurrency())) {
            amountToDeposit = amountToWithdraw;
        } else {
            BigDecimal rate = exchangeClient.getExchangeRate(from.getCurrency(), to.getCurrency());
            amountToDeposit = amountToWithdraw.multiply(rate);
        }

        accountClient.updateBalance(from.getAccountId(), UpdateBalanceRequestDto.builder()
                .amount(amountToWithdraw)
                .operationType("WITHDRAW")
                .build());

        accountClient.updateBalance(to.getAccountId(), UpdateBalanceRequestDto.builder()
                .amount(amountToDeposit)
                .operationType("DEPOSIT")
                .build());

        notificationClient.notify(NotificationRequestDto.builder()
                .userName(from.getUsername())
                .message(String.format("%s %.2f %s",
                        "WITHDRAW", request.getAmount(),
                        from.getCurrency()))
                .build());
    }

}
