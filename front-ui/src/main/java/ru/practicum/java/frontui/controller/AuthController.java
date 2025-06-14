package ru.practicum.java.frontui.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.java.frontui.dto.LoginRequestDto;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/process-login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpServletResponse response,
                        Model model) {
        try {
            LoginRequestDto dto = new LoginRequestDto(username, password);

            ResponseEntity<String> accountServiceResponse = restTemplate.postForEntity(
                    "http://api-gateway/accounts/auth/login",
                    dto,
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
            return "login";
        } catch (JsonProcessingException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

}
