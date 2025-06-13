package ru.practicum.java.exchangegeneratorservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.practicum.java.exchangegeneratorservice.client.ExchangeClient;

@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final ExchangeClient exchangeClient;

    @Scheduled(fixedRate = 1000)
    public void scheduled() {
        exchangeClient.pingExchangeService();
    }

}
