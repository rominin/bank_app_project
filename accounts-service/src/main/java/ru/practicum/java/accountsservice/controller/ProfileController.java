package ru.practicum.java.accountsservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @GetMapping("/profile")
    public ResponseEntity<String> profile(Authentication authentication) {
        return ResponseEntity.ok("Hello, " + authentication.getName());
    }

}
