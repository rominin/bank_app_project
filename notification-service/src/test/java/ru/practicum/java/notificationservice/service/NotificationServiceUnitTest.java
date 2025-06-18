package ru.practicum.java.notificationservice.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.notificationservice.dto.NotificationRequest;
import ru.practicum.java.notificationservice.model.Notification;
import ru.practicum.java.notificationservice.repository.NotificationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = NotificationService.class)
public class NotificationServiceUnitTest {

    @MockitoBean
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Test
    void sendNotification_shouldSaveNotificationToRepository() {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .userName("alice")
                .message("Your funds have been credited.")
                .build();

        // when
        notificationService.sendNotification(request);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getUserName()).isEqualTo("alice");
        assertThat(savedNotification.getMessage()).isEqualTo("Your funds have been credited.");
        assertThat(savedNotification.getCreatedAt()).isNotNull();
    }

}
