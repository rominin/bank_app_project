package ru.practicum.java.frontui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.java.frontui.client.CashClient;
import ru.practicum.java.frontui.dto.OperationRequestDto;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashClient cashClient;

    public void processOperation(OperationRequestDto operation) {
        cashClient.process(operation);
    }

}
