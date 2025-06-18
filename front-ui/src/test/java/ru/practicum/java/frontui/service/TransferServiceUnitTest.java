package ru.practicum.java.frontui.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.frontui.client.TransferClient;
import ru.practicum.java.frontui.dto.TransferRequestDto;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TransferService.class)
public class TransferServiceUnitTest {

    @MockitoBean
    private TransferClient transferClient;

    @Autowired
    private TransferService transferService;

    @Test
    void transfer_shouldCallClientWithRequest() {
        // given
        TransferRequestDto request = new TransferRequestDto();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(1000.00));

        // when
        transferService.transfer(request);

        // then
        verify(transferClient).transfer(request);
    }

}
