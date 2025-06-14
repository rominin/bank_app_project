package ru.practicum.java.accountsservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.java.accountsservice.dto.UpdateBalanceRequest;
import ru.practicum.java.accountsservice.dto.UserAccountDto;
import ru.practicum.java.accountsservice.entity.Currency;
import ru.practicum.java.accountsservice.entity.UserAccount;
import ru.practicum.java.accountsservice.repository.UserAccountRepository;
import ru.practicum.java.accountsservice.service.AccountService;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final UserAccountRepository accountRepository;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<UserAccount>> getAccounts(@RequestParam(name = "userId") Long userId) {
        return ResponseEntity.ok(accountService.getAccounts(userId));
    }

    @GetMapping("/byUsername/{username}")
    public ResponseEntity<List<UserAccount>> getAccounts(@PathVariable("username") String username) {
        return ResponseEntity.ok(accountService.getAccountsByUsername(username));
    }

    @PostMapping
    public ResponseEntity<Void> addAccount(@RequestParam(name = "userId") Long userId,
                                           @RequestParam(name = "currency") Currency currency) {
        accountService.addAccount(userId, currency);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@RequestParam(name = "userId") Long userId,
                                              @RequestParam(name = "currency") Currency currency) {
        accountService.deleteAccountByCurrency(userId, currency);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<UserAccountDto> getAccountById(@PathVariable("accountId") Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> UserAccountDto.builder()
                        .accountId(account.getId())
                        .userId(account.getUser().getId())
                        .username(account.getUser().getUsername())
                        .currency(account.getCurrency().name())
                        .balance(account.getBalance())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{accountId}/balance")
    public ResponseEntity<Void> updateAccountBalance(@PathVariable("accountId") Long accountId, @RequestBody UpdateBalanceRequest request) throws AccountNotFoundException {
        accountService.updateBalance(accountId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> editAccount(@RequestParam(name = "userId") Long userId,
                                            @RequestParam(name = "accountId") Long accountId,
                                            @RequestParam(name = "currency") Currency currency) {
        accountService.editAccount(accountId, userId, currency);
        return ResponseEntity.ok().build();
    }

}
