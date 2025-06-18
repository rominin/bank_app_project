package ru.practicum.java.accountsservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JwtUtil.class)
public class JwtUtilUnitTest {

    private final String secret = "supersecretkey123supersecretkey123";
    private final long expirationMinutes = 10;
    private final String username = "testuser";

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expirationMinutes", expirationMinutes);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(username);
        assertNotNull(token);

        boolean isValid = jwtUtil.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateToken(username);
        String extracted = jwtUtil.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    void testInvalidToken_shouldBeInvalid() {
        String invalidToken = "ey.invalid.token";
        boolean isValid = jwtUtil.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    void testExpiredToken_shouldBeInvalid() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMinutes", -1L);
        String expiredToken = jwtUtil.generateToken(username);
        boolean isValid = jwtUtil.validateToken(expiredToken);
        assertFalse(isValid);
    }

}
