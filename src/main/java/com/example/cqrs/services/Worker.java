package com.example.cqrs.services;

import com.example.cqrs.dto.WorkResultDto;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.example.cqrs.services.RequestStatus.*;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Slf4j
public class Worker implements Runnable {

    private final Map<UUID, Work> requestIdToWorkMap;
    private Work work;

    public Worker(Map<UUID, Work> requestIdToWorkMap, Work work) {
        this.requestIdToWorkMap = requestIdToWorkMap;
        this.work = work;
    }

    @Override
    public void run() {
        try {
            processQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void processQueue() throws InterruptedException {
        try {
            long threadId = Thread.currentThread().getId();
            Long delay = work.getDelay();

            log.info("Начата работа {} с задержкой {}, тред {}", work.getId(), work.getDelay(), threadId);
            Thread.sleep(delay);
            log.info("Закончена работа {}, тред {}", work.getId(), threadId);

            createSuccessWorkResult(work.getId());
        } catch (Exception e) {
            createErrorWorkResult(work.getId());
        }
    }

    private void createSuccessWorkResult(UUID id) {
        WorkResultDto workResult = WorkResultDto.builder()
                .result(format("Ok, thread %s", Thread.currentThread().getId()))
                .build();

        createWorkResult(id, READY, workResult);
    }

    private void createErrorWorkResult(UUID id) {
        createWorkResult(id, ERROR, null);
    }

    private void createWorkResult(UUID id, RequestStatus status, WorkResultDto workResult) {
        Work work = requestIdToWorkMap.get(id);
        LocalDateTime evictTime = now().plusSeconds(work.getTtl());

        requestIdToWorkMap.put(id,
                work.toBuilder()
                        .requestStatus(status)
                        .workResult(workResult)
                        .evictTime(evictTime)
                        .build());
    }
}
