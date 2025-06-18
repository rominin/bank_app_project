package ru.practicum.java.exchangeservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.exchangeservice.dto.CurrencyRateDto;
import ru.practicum.java.exchangeservice.model.ExchangeRate;
import ru.practicum.java.exchangeservice.repo.ExchangeRateRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ExchangeService.class)
public class ExchangeServiceUnitTest {

    @MockitoBean
    private ExchangeRateRepository repository;

    @Autowired
    private ExchangeService exchangeService;

    @Test
    void getRates_shouldCallRepository() {
        List<ExchangeRate> mockRates = List.of(
                new ExchangeRate(1L, "USD", "RUB", BigDecimal.valueOf(90.00), Instant.now())
        );
        when(repository.findLatestByBase("USD")).thenReturn(mockRates);

        List<ExchangeRate> result = exchangeService.getRates("USD");

        assertThat(result).isEqualTo(mockRates);
        verify(repository).findLatestByBase("USD");
    }

    @Test
    void getRate_shouldReturnOneForSameCurrency() {
        Optional<BigDecimal> rate = exchangeService.getRate("USD", "USD");
        assertThat(rate).contains(BigDecimal.ONE);
        verifyNoInteractions(repository);
    }

    @Test
    void getRate_shouldReturnRepositoryValueForDifferentCurrencies() {
        ExchangeRate rate = new ExchangeRate(1L, "USD", "EUR", BigDecimal.valueOf(0.93), Instant.now());
        when(repository.findTopByFromAndToOrderByTimestampDesc("USD", "EUR")).thenReturn(Optional.of(rate));

        Optional<BigDecimal> result = exchangeService.getRate("USD", "EUR");

        assertThat(result).contains(BigDecimal.valueOf(0.93));
        verify(repository).findTopByFromAndToOrderByTimestampDesc("USD", "EUR");
    }

    @Test
    void save_shouldConvertAndCallRepository() {
        CurrencyRateDto dto = CurrencyRateDto.builder()
                .from("USD")
                .to("RUB")
                .rate(BigDecimal.valueOf(90.00))
                .timestamp(Instant.now())
                .build();

        exchangeService.save(dto);

        verify(repository).save(argThat(rate ->
                rate.getFrom().equals("USD")
                        && rate.getTo().equals("RUB")
                        && rate.getRate().equals(BigDecimal.valueOf(90.00))
                        && rate.getTimestamp().equals(dto.getTimestamp())
        ));
    }

}
