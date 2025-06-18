package ru.practicum.java.accountsservice.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.java.accountsservice.config.NoAuthNotificationClientTestConfig;
import ru.practicum.java.accountsservice.dto.NotificationRequestDto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {NoAuthNotificationClientTestConfig.class, NotificationClient.class})
@AutoConfigureStubRunner(
        ids = "ru.practicum.java:notification-service:0.0.1-SNAPSHOT:stubs:8080",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = {
        "notification-service.url=http://localhost:8080/notify"
})
class NotificationClientContractTest {

    @Autowired
    private NotificationClient notificationClient;

    @Test
    void shouldSendNotification() {
        NotificationRequestDto dto = NotificationRequestDto.builder()
                .userName("alice")
                .message("Your funds have been credited.")
                .build();

        assertDoesNotThrow(() -> notificationClient.notify(dto));
    }
}
