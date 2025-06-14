package ru.practicum.java.frontui.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.java.frontui.client.AccountClient;
import ru.practicum.java.frontui.dto.UserAccountDto;
import ru.practicum.java.frontui.dto.UserDto;
import ru.practicum.java.frontui.dto.UserUpdateDto;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountAndProfileService {

    private final AccountClient accountClient;

    public Map<UserDto, List<UserAccountDto>> loadAccountSettings(HttpServletRequest request) {
        UserDto profile = accountClient.getUserInfo(request);
        List<UserAccountDto> accounts = accountClient.getAccounts(profile.getId());
        return Map.of(profile, accounts);
    }

    public void deleteProfile(HttpServletRequest request) {
        accountClient.deleteProfile(request);
    }

    public void changePassword(String newPassword, HttpServletRequest request) {
        accountClient.changePassword(newPassword, request);
    }

    public void updateProfile(UserUpdateDto updatedProfile, HttpServletRequest request) {
        accountClient.updateProfile(updatedProfile, request);
    }

    public void deleteAccount(Long userId, String currency) {
        accountClient.deleteAccount(userId, currency);
    }

    public void addAccount(Long userId, String currency) {
        accountClient.addAccount(userId, currency);
    }

    public void updateAccountCurrency(Long accountId, Long userId, String currency) {
        accountClient.updateAccountCurrency(accountId, userId, currency);
    }

    public List<UserAccountDto> findAccountsByUsername(String username) {
        return accountClient.findAccountsByUsername(username);
    }

}
