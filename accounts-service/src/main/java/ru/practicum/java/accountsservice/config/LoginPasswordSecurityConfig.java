package ru.practicum.java.accountsservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.practicum.java.accountsservice.security.JwtRequestFilter;
import ru.practicum.java.accountsservice.security.JwtUtil;
import ru.practicum.java.accountsservice.service.UserService;

@Configuration
@RequiredArgsConstructor
public class LoginPasswordSecurityConfig {
    // this class is for authentication&authorization through login+password
    // instead of Oauth2 Authorization Code Flow we're using here self-created account service

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/auth/**", "/profile")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(new JwtRequestFilter(jwtUtil, userService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
