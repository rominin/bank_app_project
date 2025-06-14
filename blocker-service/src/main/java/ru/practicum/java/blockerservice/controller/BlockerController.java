package ru.practicum.java.blockerservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.java.blockerservice.dto.BlockCheckRequest;
import ru.practicum.java.blockerservice.dto.BlockCheckResponse;
import ru.practicum.java.blockerservice.service.BlockerService;

@RestController
@RequiredArgsConstructor
public class BlockerController {

    private final BlockerService blockerService;

    @PostMapping("/check")
    public ResponseEntity<BlockCheckResponse> check(@Valid @RequestBody BlockCheckRequest request) {
        return ResponseEntity.ok(blockerService.check(request));
    }

}
