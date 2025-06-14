package ru.practicum.java.accountsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.service.ProfileService;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getUserProfile(user.getId()));
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
        profileService.deleteProfile(user);
        return ResponseEntity.noContent().build();
    }

}
