package ru.practicum.java.accountsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.practicum.java.accountsservice.dto.LoginRequestDto;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.repository.UserRepository;
import ru.practicum.java.accountsservice.security.JwtUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (Period.between(dto.getBirthDate(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getBirthDate())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);
    }

    public String authenticateUser(LoginRequestDto dto) {
        User user = getByUsername(dto.getUsername());

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtUtil.generateToken(user.getUsername());
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
