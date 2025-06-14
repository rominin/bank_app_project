package ru.practicum.java.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.java.notificationservice.dto.NotificationRequest;
import ru.practicum.java.notificationservice.model.Notification;
import ru.practicum.java.notificationservice.repository.NotificationRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userName(request.getUserName())
                .message(request.getMessage())
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification has been sent for user {}: {}", request.getUserName(), request.getMessage());
    }
}
