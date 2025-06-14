package ru.practicum.java.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.java.notificationservice.dto.NotificationRequest;
import ru.practicum.java.notificationservice.service.NotificationService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/notify")
    public ResponseEntity<Void> notify(@Valid @RequestBody NotificationRequest request) {
        log.info("Incoming notification for user: {}", request.getUserName());
        notificationService.sendNotification(request);
        return ResponseEntity.ok().build();
    }

}
