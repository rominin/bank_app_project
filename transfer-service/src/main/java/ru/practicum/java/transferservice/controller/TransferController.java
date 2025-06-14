package ru.practicum.java.transferservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.java.transferservice.dto.TransferRequestDto;
import ru.practicum.java.transferservice.service.TransferService;

@RestController
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<String> transfer(@RequestBody TransferRequestDto request) {
        transferService.transfer(request);
        return ResponseEntity.ok().build();
    }

}
