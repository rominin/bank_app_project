package ru.practicum.java.frontui.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.frontui.dto.LoginRequestDto;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    @Value("${account-service.general-url}")
    private String accountServiceUrl;

    private final RestTemplate restTemplate;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName,
                           @RequestParam("email") String email,
                           @RequestParam("birthDate") String birthDate,
                           HttpServletResponse response,
                           Model model) {

        Map<String, Object> dto = Map.of(
                "username", username,
                "password", password,
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "birthDate", birthDate // формат: "yyyy-MM-dd"
        );

        try {
            restTemplate.postForEntity(accountServiceUrl + "/auth/register", dto, Void.class);

            LoginRequestDto loginRequestDto = new LoginRequestDto(username, password);
            ResponseEntity<String> accountServiceResponse = restTemplate.postForEntity(
                    accountServiceUrl + "/auth/login",
                    loginRequestDto,
                    String.class
            );
            String jwt = new ObjectMapper()
                    .readTree(accountServiceResponse.getBody())
                    .get("token")
                    .asText();
            Cookie cookie = new Cookie("JWT", jwt);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            return "redirect:/home";
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
            return "register";
        } catch (JsonProcessingException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

    }
}
