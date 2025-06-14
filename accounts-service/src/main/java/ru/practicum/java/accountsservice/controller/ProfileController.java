package ru.practicum.java.accountsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.Currency;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.entity.UserAccount;
import ru.practicum.java.accountsservice.service.AccountProfileService;

import java.util.List;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AccountProfileService profileService;

    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getUserProfile(user));
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User user,
                                              @RequestBody @Valid UserRegistrationDto dto) {
        profileService.updateProfile(user, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                               @RequestBody String newPassword) {
        profileService.changePassword(user, newPassword);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal User user) {
        profileService.deleteAccount(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<UserAccount>> getAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getAccounts(user));
    }

    @PostMapping("/accounts")
    public ResponseEntity<Void> addAccount(@AuthenticationPrincipal User user,
                                           @RequestParam(name = "currency")  Currency currency) {
        profileService.addAccount(user, currency);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/accounts")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal User user,
                                              @RequestParam(name = "currency") Currency currency) {
        profileService.deleteAccountByCurrency(user, currency);
        return ResponseEntity.noContent().build();
    }

}
