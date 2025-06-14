package ru.practicum.java.frontui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.java.frontui.client.TransferClient;
import ru.practicum.java.frontui.dto.TransferRequestDto;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferClient transferClient;

    public void transfer(TransferRequestDto request) {
        transferClient.transfer(request);
    }

}
