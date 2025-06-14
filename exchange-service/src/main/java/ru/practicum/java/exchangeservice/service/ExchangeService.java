package ru.practicum.java.exchangeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.java.exchangeservice.dto.CurrencyRateDto;
import ru.practicum.java.exchangeservice.model.ExchangeRate;
import ru.practicum.java.exchangeservice.repo.ExchangeRateRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private final ExchangeRateRepository repository;

    public List<ExchangeRate> getRates(String base) {
        return repository.findLatestByBase(base);
    }

    public Optional<BigDecimal> getRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return Optional.of(BigDecimal.ONE);
        }
        return repository.findTopByFromAndToOrderByTimestampDesc(from, to)
                .map(ExchangeRate::getRate);
    }

    public void save(CurrencyRateDto dto) {
        ExchangeRate rate = ExchangeRate.builder()
                .from(dto.getFrom())
                .to(dto.getTo())
                .rate(dto.getRate())
                .timestamp(dto.getTimestamp())
                .build();
        repository.save(rate);
    }
}
