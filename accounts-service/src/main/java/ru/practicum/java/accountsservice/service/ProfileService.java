package ru.practicum.java.accountsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.java.accountsservice.client.NotificationClient;
import ru.practicum.java.accountsservice.dto.NotificationRequestDto;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.repository.UserAccountRepository;
import ru.practicum.java.accountsservice.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;

    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
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
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Пароль изменён.").build());
    }

    public void deleteProfile(User user) {
        boolean hasNonZeroBalance = userAccountRepository.existsByUserAndBalanceGreaterThan(user, BigDecimal.ZERO);
        if (hasNonZeroBalance) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete user with non-zero balances");
        }
        userRepository.delete(user);
        notificationClient.notify(NotificationRequestDto.builder().userName(user.getUsername()).message("Счёт удалён.").build());
    }

}
