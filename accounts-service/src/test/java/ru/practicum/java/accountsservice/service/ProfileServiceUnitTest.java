package ru.practicum.java.accountsservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.java.accountsservice.client.NotificationClient;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.repository.UserAccountRepository;
import ru.practicum.java.accountsservice.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ProfileService.class)
public class ProfileServiceUnitTest {

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserAccountRepository userAccountRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private NotificationClient notificationClient;

    @Autowired
    private ProfileService profileService;

    @Test
    void getUserProfile_found() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = profileService.getUserProfile(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getUserProfile_notFound_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> profileService.getUserProfile(1L));
    }

    @Test
    void updateProfile_shouldUpdateFieldsAndNotify() {
        User user = new User();
        user.setUsername("john");

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");

        profileService.updateProfile(user, dto);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.getUpdatedAt());

        verify(userRepository).save(user);
        verify(notificationClient).notify(argThat(n ->
                n.getUserName().equals("john") &&
                        n.getMessage().contains("Профиль обновлён")
        ));
    }

    @Test
    void changePassword_shouldEncodeAndNotify() {
        User user = new User();
        user.setUsername("alice");

        when(passwordEncoder.encode("newpass")).thenReturn("encodedPass");

        profileService.changePassword(user, "newpass");

        assertEquals("encodedPass", user.getPassword());
        assertNotNull(user.getUpdatedAt());

        verify(userRepository).save(user);
        verify(notificationClient).notify(argThat(n ->
                n.getUserName().equals("alice") &&
                        n.getMessage().contains("Пароль изменён")
        ));
    }

    @Test
    void changePassword_empty_shouldThrow() {
        User user = new User();

        assertThrows(ResponseStatusException.class, () -> profileService.changePassword(user, " "));
    }

    @Test
    void deleteProfile_zeroBalance_shouldDeleteAndNotify() {
        User user = new User();
        user.setUsername("bob");

        when(userAccountRepository.existsByUserAndBalanceGreaterThan(eq(user), eq(BigDecimal.ZERO)))
                .thenReturn(false);

        profileService.deleteProfile(user);

        verify(userRepository).delete(user);
        verify(notificationClient).notify(argThat(n ->
                n.getUserName().equals("bob") &&
                        n.getMessage().contains("Счёт удалён")
        ));
    }

    @Test
    void deleteProfile_nonZeroBalance_shouldThrow() {
        User user = new User();

        when(userAccountRepository.existsByUserAndBalanceGreaterThan(eq(user), eq(BigDecimal.ZERO)))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.deleteProfile(user));
        assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getBody().getStatus());
    }

}
