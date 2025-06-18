package ru.practicum.java.frontui.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.frontui.client.ExchangeClient;
import ru.practicum.java.frontui.dto.ExchangeRateResponseDto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ExchangeService.class)
public class ExchangeServiceUnitTest {

    @MockitoBean
    private ExchangeClient exchangeClient;

    @Autowired
    private ExchangeService exchangeService;

    @Test
    void getRates_shouldReturnRatesFromClient() {
        // given
        String base = "RUB";
        List<ExchangeRateResponseDto> expectedRates = List.of(
                new ExchangeRateResponseDto("RUB", "USD", BigDecimal.valueOf(85.0)),
                new ExchangeRateResponseDto("RUB", "EUR", BigDecimal.valueOf(93.0))
        );

        when(exchangeClient.getRates(base)).thenReturn(expectedRates);

        // when
        List<ExchangeRateResponseDto> actualRates = exchangeService.getRates(base);

        // then
        assertEquals(expectedRates, actualRates);
        verify(exchangeClient).getRates(base);
    }

}
