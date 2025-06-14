package ru.practicum.java.frontui.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.practicum.java.frontui.dto.*;
import ru.practicum.java.frontui.service.AccountAndProfileService;
import ru.practicum.java.frontui.service.CashService;
import ru.practicum.java.frontui.service.ExchangeService;
import ru.practicum.java.frontui.service.TransferService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final AccountAndProfileService accountAndProfileService;
    private final CashService cashService;
    private final TransferService transferService;
    private final ExchangeService exchangeService;

    @GetMapping
    public String home(@RequestParam(required = false, name = "recipientUsername") String recipientUsername,
                       @RequestParam(required = false, name = "fromAccountId") Long fromAccountId,
                       HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "home-anonymous";
        }

        Map<UserDto, List<UserAccountDto>> userInfo = accountAndProfileService.loadAccountSettings(request);
        UserDto profile = userInfo.keySet().iterator().next();
        List<UserAccountDto> accounts = userInfo.get(profile);

        model.addAttribute("userId", profile.getId());
        model.addAttribute("username", profile.getUsername());
        model.addAttribute("firstName", profile.getFirstName());
        model.addAttribute("lastName", profile.getLastName());
        model.addAttribute("email", profile.getEmail());
        model.addAttribute("birthDate", profile.getDateOfBirth().toString());
        model.addAttribute("accounts", accounts);

        List<String> bases = List.of("RUB", "USD", "EUR", "CNY");

        Map<String, Map<String, BigDecimal>> exchangeTable = new LinkedHashMap<>();

        for (String base : bases) {
            List<ExchangeRateResponseDto> rates = exchangeService.getRates(base);
            Map<String, BigDecimal> row = rates.stream()
                    .collect(Collectors.toMap(ExchangeRateResponseDto::getTo, ExchangeRateResponseDto::getRate));
            exchangeTable.put(base, row);
        }

        model.addAttribute("exchangeTable", exchangeTable);

        if (recipientUsername != null && fromAccountId != null) {
            model.addAttribute("recipientUsername", recipientUsername);
            model.addAttribute("fromAccountId", fromAccountId);

            try {
                List<UserAccountDto> recipientAccounts = accountAndProfileService.findAccountsByUsername(recipientUsername);
                if (recipientAccounts.isEmpty()) {
                    model.addAttribute("recipientError", "У пользователя нет доступных счетов.");
                } else {
                    model.addAttribute("recipientAccounts", recipientAccounts);
                }
            } catch (Exception e) {
                model.addAttribute("recipientError", "Пользователь не найден.");
            }
        }

        return "home";
    }

    @PostMapping("/deleteProfile")
    public String deleteProfile(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            accountAndProfileService.deleteProfile(request);

            Cookie cookie = new Cookie("JWT", null);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            SecurityContextHolder.clearContext();

            return "redirect:/login?logout";
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                redirectAttributes.addFlashAttribute("deleteError", "Невозможно удалить профиль: есть ненулевые счета.");
                return "redirect:/home";
            } else {
                throw ex;
            }
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam(name = "newPassword") String newPassword, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.isEmpty()) {
            redirectAttributes.addAttribute("passwordError", "Password cannot be empty.");
            return "redirect:/home";
        }

        try {
            accountAndProfileService.changePassword(newPassword, request);
            redirectAttributes.addAttribute("passwordSuccess", "Password has been changed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }

        return "redirect:/home";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(@RequestParam("firstName") String firstName,
                                @RequestParam("lastName") String lastName,
                                @RequestParam("email") String email,
                                @RequestParam("birthDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (ChronoUnit.YEARS.between(birthDate, LocalDate.now()) < 18) {
            redirectAttributes.addFlashAttribute("updateProfileError", "Возраст должен быть больше 18 лет.");
            return "redirect:/home";
        }

        try {
            accountAndProfileService.updateProfile(new UserUpdateDto("null", "null", firstName, lastName, email, birthDate), request);
            redirectAttributes.addFlashAttribute("updateProfileSuccess", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("updateProfileError", "Ошибка при обновлении профиля.");
        }

        return "redirect:/home";
    }

    @PostMapping("/accounts/delete")
    public String deleteAccount(@RequestParam("userId") Long userId,
                                @RequestParam("currency") String currency,
                                RedirectAttributes redirectAttributes) {
        try {
            accountAndProfileService.deleteAccount(userId, currency);
            return "redirect:/home";
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                redirectAttributes.addFlashAttribute("deleteAccountError", "Не удалось удалить счёт: " + ex.getMessage());
                return "redirect:/home";
            } else
                throw ex;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("deleteAccountError", e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/accounts/add")
    public String addAccount(@RequestParam("userId") Long userId,
                                @RequestParam("currency") String currency,
                                RedirectAttributes redirectAttributes) {
        try {
            accountAndProfileService.addAccount(userId, currency);
            return "redirect:/home";
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                redirectAttributes.addFlashAttribute("addAccountError", "Error by account creation: " + ex.getMessage());
                return "redirect:/home";
            } else
                throw ex;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("addAccountError", e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/accounts/edit")
    public String editAccount(@RequestParam("userId") Long userId,
                             @RequestParam("accountId") Long accountId,
                             @RequestParam("currency") String currency,
                             RedirectAttributes redirectAttributes) {
        try {
            accountAndProfileService.updateAccountCurrency(accountId, userId, currency);
            return "redirect:/home";
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                redirectAttributes.addFlashAttribute("editAccountError", "Error by account editing: " + ex.getMessage());
                return "redirect:/home";
            } else
                throw ex;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("editAccountError", e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/cash")
    public String processCashOperation(
            @RequestParam("accountId") Long accountId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("operationType") String operationType,
            RedirectAttributes redirectAttributes
    ) {
        try {
            OperationRequestDto dto = OperationRequestDto.builder()
                    .accountId(accountId)
                    .amount(amount)
                    .operationType(operationType)
                    .build();

            cashService.processOperation(dto);

            redirectAttributes.addFlashAttribute("cashSuccess", operationType.equals("DEPOSIT")
                    ? "Средства успешно внесены"
                    : "Средства успешно сняты");
        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("cashError", e.getStatusCode() == HttpStatus.BAD_REQUEST
                    ? "Недостаточно средств для снятия" : "Ошибка при выполнении операции");
        }

        return "redirect:/home";
    }

    @PostMapping("/transfer")
    public String selfTransfer(@ModelAttribute TransferRequestDto request,
                               RedirectAttributes redirectAttributes) {
        try {
            transferService.transfer(request);
            redirectAttributes.addFlashAttribute("selfTransferSuccess", "Перевод выполнен успешно!");
        } catch (HttpClientErrorException.BadRequest e) {
            redirectAttributes.addFlashAttribute("selfTransferError", "Ошибка: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("selfTransferError", "Непредвиденная ошибка: " + e.getMessage());
        }
        return "redirect:/home";
    }

    @PostMapping("/transferToUser")
    public String transferToUser(@RequestParam("fromAccountId") Long fromAccountId,
                                 @RequestParam("toAccountId") Long toAccountId,
                                 @RequestParam("amount") BigDecimal amount,
                                 RedirectAttributes redirectAttributes) {
        try {
            transferService.transfer(new TransferRequestDto(fromAccountId, toAccountId, amount));
            redirectAttributes.addFlashAttribute("transferToUserSuccess", "Перевод успешно выполнен!");
        } catch (HttpClientErrorException.BadRequest e) {
            redirectAttributes.addFlashAttribute("transferToUserError", "Ошибка: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("transferToUserError", "Непредвиденная ошибка: " + e.getMessage());
        }
        return "redirect:/home";
    }

}
