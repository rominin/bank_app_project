package ru.practicum.java.accountsservice.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.accountsservice.dto.LoginRequestDto;
import ru.practicum.java.accountsservice.dto.UserRegistrationDto;
import ru.practicum.java.accountsservice.entity.User;
import ru.practicum.java.accountsservice.repository.UserRepository;
import ru.practicum.java.accountsservice.security.JwtUtil;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserService.class)
public class UserServiceUnitTest {

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Test
    void registerUser_shouldSave_whenValid() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("john");
        dto.setEmail("john@example.com");
        dto.setPassword("123456");
        dto.setBirthDate(LocalDate.now().minusYears(20));
        dto.setFirstName("John");
        dto.setLastName("Doe");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");

        userService.registerUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("john", saved.getUsername());
        assertEquals("encoded", saved.getPassword());
        assertEquals("john@example.com", saved.getEmail());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void registerUser_shouldThrow_ifUsernameExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("existing");
        dto.setEmail("mail@example.com");
        dto.setBirthDate(LocalDate.now().minusYears(20));

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void registerUser_shouldThrow_ifEmailExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("existing@example.com");
        dto.setBirthDate(LocalDate.now().minusYears(20));

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void registerUser_shouldThrow_ifUnderage() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("young");
        dto.setEmail("young@example.com");
        dto.setBirthDate(LocalDate.now().minusYears(17));

        when(userRepository.existsByUsername("young")).thenReturn(false);
        when(userRepository.existsByEmail("young@example.com")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    void authenticateUser_shouldReturnToken_ifCredentialsMatch() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername("john");
        dto.setPassword("password");

        User user = new User();
        user.setUsername("john");
        user.setPassword("encoded");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("john")).thenReturn("jwt-token");

        String token = userService.authenticateUser(dto);

        assertEquals("jwt-token", token);
    }

    @Test
    void authenticateUser_shouldThrow_ifPasswordInvalid() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername("john");
        dto.setPassword("wrong");

        User user = new User();
        user.setUsername("john");
        user.setPassword("encoded");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.authenticateUser(dto));
    }

    @Test
    void getByUsername_shouldReturnUser_ifExists() {
        User user = new User();
        user.setUsername("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        User result = userService.getByUsername("john");

        assertEquals("john", result.getUsername());
    }

    @Test
    void getByUsername_shouldThrow_ifNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getByUsername("ghost"));
    }

}
