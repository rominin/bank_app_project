package ru.practicum.java.frontui.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.frontui.client.AccountClient;
import ru.practicum.java.frontui.dto.UserAccountDto;
import ru.practicum.java.frontui.dto.UserDto;
import ru.practicum.java.frontui.dto.UserUpdateDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AccountAndProfileService.class)
public class AccountAndProfileServiceUnitTest {

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private HttpServletRequest request;

    @Autowired
    private AccountAndProfileService service;

    @Test
    void loadAccountSettings_shouldReturnProfileAndAccounts() {
        UserDto userDto = new UserDto(1L, "Alex", "alex@test.com", "", "", LocalDate.now());
        List<UserAccountDto> accounts = List.of(
                new UserAccountDto(1L, userDto, "RUB", BigDecimal.valueOf(1000)),
                new UserAccountDto(2L, userDto, "USD", BigDecimal.valueOf(200))
        );

        when(accountClient.getUserInfo(request)).thenReturn(userDto);
        when(accountClient.getAccounts(userDto.getId())).thenReturn(accounts);

        Map<UserDto, List<UserAccountDto>> result = service.loadAccountSettings(request);

        assertEquals(1, result.size());
        assertEquals(accounts, result.get(userDto));
        verify(accountClient).getUserInfo(request);
        verify(accountClient).getAccounts(userDto.getId());
    }

    @Test
    void deleteProfile_shouldCallClient() {
        service.deleteProfile(request);
        verify(accountClient).deleteProfile(request);
    }

    @Test
    void changePassword_shouldCallClient() {
        String newPassword = "newpass123";
        service.changePassword(newPassword, request);
        verify(accountClient).changePassword(newPassword, request);
    }

    @Test
    void updateProfile_shouldCallClient() {
        UserUpdateDto dto = new UserUpdateDto("newName", "new@email.com", "", "", "", LocalDate.now());
        service.updateProfile(dto, request);
        verify(accountClient).updateProfile(dto, request);
    }

    @Test
    void deleteAccount_shouldCallClient() {
        service.deleteAccount(1L, "USD");
        verify(accountClient).deleteAccount(1L, "USD");
    }

    @Test
    void addAccount_shouldCallClient() {
        service.addAccount(2L, "EUR");
        verify(accountClient).addAccount(2L, "EUR");
    }

    @Test
    void updateAccountCurrency_shouldCallClient() {
        service.updateAccountCurrency(3L, 1L, "CNY");
        verify(accountClient).updateAccountCurrency(3L, 1L, "CNY");
    }

    @Test
    void findAccountsByUsername_shouldReturnAccounts() {
        String username = "alex";
        List<UserAccountDto> accounts = List.of(new UserAccountDto(1L, new UserDto(1L, username, "", "", "", LocalDate.now()), "RUB", BigDecimal.TEN));
        when(accountClient.findAccountsByUsername(username)).thenReturn(accounts);

        List<UserAccountDto> result = service.findAccountsByUsername(username);
        assertEquals(accounts, result);
        verify(accountClient).findAccountsByUsername(username);
    }

}
