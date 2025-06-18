package ru.practicum.java.exchangegeneratorservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.exchangegeneratorservice.client.ExchangeClient;
import ru.practicum.java.exchangegeneratorservice.config.CurrencyProperties;
import ru.practicum.java.exchangegeneratorservice.dto.CurrencyRateDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RateGenerationService.class)
public class RateGenerationServiceUnitTest {

    @MockitoBean
    private CurrencyProperties properties;
    @MockitoBean
    private ExchangeClient exchangeClient;

    @Autowired
    private RateGenerationService service;

    @Test
    void testGenerateRates_shouldCallExchangeClient() {
        when(properties.getPairs()).thenReturn(List.of("USD", "EUR", "CNY"));
        when(properties.getRange()).thenReturn(Map.of(
                "USD", new CurrencyProperties.Range(80, 90),
                "EUR", new CurrencyProperties.Range(85, 100),
                "CNY", new CurrencyProperties.Range(11, 11.5)
        ));

        service.generateRates();

        verify(exchangeClient, atLeast(12)).send(any(CurrencyRateDto.class));
    }
}
