package ru.practicum.java.cashservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.java.cashservice.dto.OperationRequestDto;
import ru.practicum.java.cashservice.service.CashService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CashController {

    private final CashService cashService;

    @PostMapping
    public ResponseEntity<Void> processOperation(@RequestBody @Valid OperationRequestDto request) {
        log.info("Запрос операции: {} {} (accountId: {})", request.getOperationType(), request.getAmount(), request.getAccountId());
        cashService.process(request);
        return ResponseEntity.ok().build();
    }

}
