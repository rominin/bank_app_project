package ru.practicum.java.frontui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.java.frontui.client.ExchangeClient;
import ru.practicum.java.frontui.dto.ExchangeRateResponseDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeClient exchangeClient;

    public List<ExchangeRateResponseDto> getRates(String base) {
        return exchangeClient.getRates(base);
    }

}
