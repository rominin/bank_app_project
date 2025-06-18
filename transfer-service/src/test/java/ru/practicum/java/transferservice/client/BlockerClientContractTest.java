package ru.practicum.java.transferservice.client;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.java.transferservice.config.NoAuthRestTemplateTestConfig;
import ru.practicum.java.transferservice.dto.BlockCheckRequestDto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {NoAuthRestTemplateTestConfig.class, BlockerClient.class})
@AutoConfigureStubRunner(
        ids = "ru.practicum.java:blocker-service:0.0.1-SNAPSHOT:stubs:8081",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = {
        "blocker-service.url=http://localhost:8081/check"
})
class BlockerClientContractTest {

    @Autowired
    private BlockerClient blockerClient;

    @Test
    void shouldReturnBlockedTrue() {
        BlockCheckRequestDto request = BlockCheckRequestDto.builder()
                .operationType("WITHDRAW")
                .userId("42")
                .amount(new BigDecimal("150000.00"))
                .build();

        boolean blocked = blockerClient.isBlocked(request);

        assertTrue(blocked, "Expected the operation to be blocked");
    }
}
