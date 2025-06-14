package ru.practicum.java.accountsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.java.accountsservice.client.NotificationClient;
import ru.practicum.java.accountsservice.dto.NotificationRequestDto;
import ru.practicum.java.accountsservice.dto.UpdateBalanceRequest;
import ru.practicum.java.accountsservice.entity.Currency;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.entity.UserAccount;
import ru.practicum.java.accountsservice.repository.UserAccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserAccountRepository userAccountRepository;
    private final NotificationClient notificationClient;
    private final ProfileService profileService;
    private final UserService userService;

    public List<UserAccount> getAccounts(Long userId) {
        User user = profileService.getUserProfile(userId);
        return userAccountRepository.findAllByUser(user);
    }

    public List<UserAccount> getAccountsByUsername(String username) {
        User user = userService.getByUsername(username);
        return userAccountRepository.findAllByUser(user);
    }

    public void addAccount(Long userId, Currency currency) {
        User user = profileService.getUserProfile(userId);
        if (userAccountRepository.findByUserAndCurrency(user, currency).isPresent()) {
            throw new IllegalStateException("Account in this currency already exists");
        }
        userAccountRepository.save(UserAccount.builder()
                .user(user)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build());
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Счёт добавлен.").build());
    }

    public void deleteAccountByCurrency(Long userId, Currency currency) {
        User user = profileService.getUserProfile(userId);
        var account = userAccountRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Account has non-zero balance"));
        }
        userAccountRepository.delete(account);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Счёт удалён.").build());
    }

    public void updateBalance(Long accountId, UpdateBalanceRequest dto) {
        UserAccount account = userAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        BigDecimal current = account.getBalance();
        BigDecimal result;

        switch (dto.getOperationType()) {
            case "DEPOSIT" -> result = current.add(dto.getAmount());

            case "WITHDRAW" -> {
                if (current.compareTo(dto.getAmount()) < 0) {
                    throw new IllegalArgumentException("Insufficient funds");
                }
                result = current.subtract(dto.getAmount());
            }

            default -> throw new IllegalArgumentException("Unsupported operation type");
        }

        account.setBalance(result);
        userAccountRepository.save(account);
    }

    public void editAccount(Long accountId, Long userId, Currency currency) {
        UserAccount account = userAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Попытка изменить чужой счёт"));
        }

        User user = profileService.getUserProfile(userId);
        if (userAccountRepository.findByUserAndCurrency(user, currency).isPresent()) {
            throw new IllegalStateException("Account in this currency already exists");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ("Account has non-zero balance"));
        }

        account.setCurrency(currency);
        userAccountRepository.save(account);
    }

}
