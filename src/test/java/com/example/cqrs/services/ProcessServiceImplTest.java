package com.example.cqrs.services;

import com.example.cqrs.dto.WorkDto;
import com.example.cqrs.utils.Parameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.UUID;

import static com.example.cqrs.services.RequestStatus.NOT_FOUND;
import static com.example.cqrs.services.RequestStatus.PROCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

class ProcessServiceImplTest {

    @Mock Parameters parameters;

    @InjectMocks
    private ProcessServiceImpl processService;

    AutoCloseable closeable;

    @BeforeEach
    public void init() {
        closeable = openMocks(this);
    }

    @AfterEach
    public void afterAll() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("Пробуем получить статус по неизвестному id")
    void test_10() {
        //given
        UUID requestId = UUID.randomUUID();

        //when
        RequestStatus result = processService.getRequestStatus(requestId);

        //then
        assertEquals(NOT_FOUND, result);
    }

    @Test
    @DisplayName("Пробуем получить результат по неизвестному id")
    void test_20() {
        //given
        UUID requestId = UUID.randomUUID();

        //when
        RuntimeException e = assertThrows(RuntimeException.class, () -> processService.getResult(requestId));

        //then
        assertEquals("Неизвестный requestId", e.getMessage());
    }

    @Test
    @DisplayName("Добавим работу и получим результат не дожидаясь выполнения")
    void test_30() {
        //given
        WorkDto workDto = WorkDto.builder().delay(10L).build();

        //when
        UUID requestId = processService.addWork(workDto);
        RequestStatus requestStatus = processService.getRequestStatus(requestId);
        RuntimeException e = assertThrows(RuntimeException.class, () -> processService.getResult(requestId));

        //then
        assertNotNull(requestId);
        assertEquals(PROCESS, requestStatus);
        assertEquals("Результат не готов", e.getMessage());
    }
}