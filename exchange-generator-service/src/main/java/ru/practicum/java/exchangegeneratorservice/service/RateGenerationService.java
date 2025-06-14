package ru.practicum.java.exchangegeneratorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.practicum.java.exchangegeneratorservice.client.ExchangeClient;
import ru.practicum.java.exchangegeneratorservice.config.CurrencyProperties;
import ru.practicum.java.exchangegeneratorservice.dto.CurrencyRateDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RateGenerationService {

    private final CurrencyProperties properties;
    private final ExchangeClient exchangeClient;

    @Scheduled(fixedRate = 10000)
    public void generateRates() {
        List<String> currencies = properties.getPairs();
        Map<String, CurrencyProperties.Range> rangeMap = properties.getRange();
        Instant now = Instant.now();

        Map<String, BigDecimal> currencyToRub = new HashMap<>();

        generateRubPairs(currencies, rangeMap, currencyToRub, now);
        generateCrossPairs(currencies, currencyToRub, now);
    }

    private void generateRubPairs(List<String> currencies, Map<String, CurrencyProperties.Range> rangeMap, Map<String, BigDecimal> currencyToRub, Instant now) {
        for (String currency : currencies) {
            CurrencyProperties.Range range = rangeMap.get(currency);
            double rubRate = getRandom(range.getMin(), range.getMax());

            BigDecimal rateDecimal = BigDecimal.valueOf(rubRate).setScale(10, RoundingMode.HALF_UP);
            currencyToRub.put(currency, rateDecimal);


            send(currency, "RUB", rateDecimal, now);


            BigDecimal inverse = BigDecimal.ONE.divide(rateDecimal, 10, RoundingMode.HALF_UP);
            send("RUB", currency, inverse, now);
        }
    }

    private void generateCrossPairs(List<String> currencies, Map<String, BigDecimal> currencyToRub, Instant now) {
        for (String from : currencies) {
            for (String to : currencies) {
                if (!from.equals(to)) {
                    BigDecimal fromToRub = currencyToRub.get(from);
                    BigDecimal toToRub = currencyToRub.get(to);

                    BigDecimal cross = fromToRub.divide(toToRub, 10, RoundingMode.HALF_UP);
                    send(from, to, cross, now);
                }
            }
        }
    }

    private void send(String from, String to, BigDecimal rate, Instant timestamp) {
        CurrencyRateDto dto = CurrencyRateDto.builder()
                .from(from)
                .to(to)
                .rate(rate)
                .timestamp(timestamp)
                .build();
        exchangeClient.send(dto);
    }

    private double getRandom(double min, double max) {
        return Math.round((min + Math.random() * (max - min)) * 10000.0) / 10000.0;
    }
}
