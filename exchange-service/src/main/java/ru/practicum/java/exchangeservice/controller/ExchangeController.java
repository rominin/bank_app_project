package ru.practicum.java.exchangeservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.java.exchangeservice.dto.CurrencyRateDto;
import ru.practicum.java.exchangeservice.dto.ExchangeRateResponseDto;
import ru.practicum.java.exchangeservice.model.ExchangeRate;
import ru.practicum.java.exchangeservice.service.ExchangeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {

    private final ExchangeService service;

    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRate>> getRates(@RequestParam(name = "base", defaultValue = "RUB") String base) {
        log.info("Получен запрос на все курсы от: {}", base);
        return ResponseEntity.ok(service.getRates(base));
    }

    @GetMapping("/rate")
    public ResponseEntity<ExchangeRateResponseDto> getRate(
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to
    ) {
        log.info("Получен запрос на курс: {} → {}", from, to);
        return service.getRate(from, to)
                .map(rate -> ResponseEntity.ok(new ExchangeRateResponseDto(rate)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rates")
    public ResponseEntity<Void> save(@RequestBody CurrencyRateDto dto) {
        log.info("Сохраняется курс: {} → {} = {}", dto.getFrom(), dto.getTo(), dto.getRate());
        service.save(dto);
        return ResponseEntity.ok().build();
    }
}
