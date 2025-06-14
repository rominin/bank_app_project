package ru.practicum.java.accountsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.practicum.java.accountsservice.client.NotificationClient;
import ru.practicum.java.accountsservice.dto.NotificationRequestDto;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.Currency;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.entity.UserAccount;
import ru.practicum.java.accountsservice.repository.UserAccountRepository;
import ru.practicum.java.accountsservice.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountProfileService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

    public User getUserProfile(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void updateProfile(User user, UserRegistrationDto dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setDateOfBirth(dto.getBirthDate());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Профиль обновлён.").build());
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Пароль изменён.").build());
    }

    public void deleteAccount(User user) {
        boolean hasNonZeroBalance = userAccountRepository.existsByUserAndBalanceGreaterThan(user, BigDecimal.ZERO);
        if (hasNonZeroBalance) {
            throw new IllegalStateException("Cannot delete user with non-zero balances");
        }
        userRepository.delete(user);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Счёт удалён.").build());
    }

    public List<UserAccount> getAccounts(User user) {
        return userAccountRepository.findAllByUser(user);
    }

    public void addAccount(User user, Currency currency) {
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

    public void deleteAccountByCurrency(User user, Currency currency) {
        var account = userAccountRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Account has non-zero balance");
        }
        userAccountRepository.delete(account);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Счёт удалён.").build());
    }

}
