package ru.practicum.java.frontui.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.frontui.client.CashClient;
import ru.practicum.java.frontui.dto.OperationRequestDto;

import static org.mockito.Mockito.verify;

@SpringBootTest(classes = CashService.class)
public class CashServiceUnitTest {

    @MockitoBean
    private CashClient cashClient;

    @Autowired
    private CashService cashService;

    @Test
    void processOperation_shouldCallClient() {
        OperationRequestDto dto = new OperationRequestDto();

        cashService.processOperation(dto);

        verify(cashClient).process(dto);
    }

}
