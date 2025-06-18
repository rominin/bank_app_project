package ru.practicum.java.notificationservice;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.java.notificationservice.controller.NotificationController;
import ru.practicum.java.notificationservice.repository.NotificationRepository;
import ru.practicum.java.notificationservice.service.NotificationService;

@WebMvcTest(NotificationController.class)
@Import({NotificationService.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public abstract class BaseContractTest {

    @MockitoBean
    private NotificationRepository notificationRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

}
