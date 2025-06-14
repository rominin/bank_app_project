package ru.practicum.java.accountsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.java.accountsservice.dto.AuthResponseDto;
import ru.practicum.java.accountsservice.dto.LoginRequestDto;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.service.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegistrationDto dto) {
        userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto dto) {
        String token = userService.authenticateUser(dto);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }
}
