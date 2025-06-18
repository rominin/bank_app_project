package ru.practicum.java.blockerservice;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.java.blockerservice.controller.BlockerController;
import ru.practicum.java.blockerservice.service.BlockerService;

@WebMvcTest(BlockerController.class)
@Import({BlockerService.class, TestSecurityConfig.class})
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public abstract class BaseContractTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

}
