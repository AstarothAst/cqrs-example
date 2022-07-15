package com.example.cqrs.services;

import com.example.cqrs.dto.WorkResultDto;
import com.example.cqrs.other.OtherServiceImpl;
import com.example.cqrs.other.RequestScopeBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.cqrs.services.RequestStatus.ERROR;
import static com.example.cqrs.services.RequestStatus.READY;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
public class Worker implements Runnable {

    private final Map<UUID, Work> requestIdToWorkMap;
    private final Work work;
    private final OtherServiceImpl otherService;
    private RequestScopeBean requestScopeBean;

    private final RequestAttributes requestAttributes;
    private Thread thread;

    public Worker(Map<UUID, Work> requestIdToWorkMap,
                  Work work,
                  OtherServiceImpl otherService,
                  RequestScopeBean requestScopeBean) {
        this.requestIdToWorkMap = requestIdToWorkMap;
        this.work = work;
        this.otherService = otherService;
        this.requestScopeBean = requestScopeBean;

        this.requestAttributes = RequestContextHolder.getRequestAttributes();
        this.thread = Thread.currentThread();
    }

    @Override
    public void run() {
        try {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            requestScopeBean = getRequestScopeBeanFromRequestAttributes();

            processQueue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (Thread.currentThread() != thread) {
                RequestContextHolder.resetRequestAttributes();
            }
            thread = null;
        }
    }

    private void processQueue() throws InterruptedException {
        try {
            long threadId = Thread.currentThread().getId();
            Long delay = work.getDelay();

            log.info("[{}] 1. начата работа {} с задержкой {}", threadId, work.getId(), work.getDelay());

            log.info("[{}] 2. {}", threadId, otherService.getSomeString());

            log.info("[{}] 3. {}", threadId, requestScopeBean.getStr());

            TimeUnit.SECONDS.sleep(delay);

            log.info("[{}] 4. закончена работа {}: {}", threadId, work.getId(), requestScopeBean.getStr());

            createSuccessWorkResult(work.getId());
        } catch (Exception e) {
            log.error("[{}] Error: {}", Thread.currentThread().getId(), e.getMessage());
            createErrorWorkResult(work.getId());
        }
    }

    private RequestScopeBean getRequestScopeBeanFromRequestAttributes() {
        return ofNullable(RequestContextHolder.getRequestAttributes())
                .map(attributes -> attributes.getAttribute("scopedTarget.requestScopeBeanImpl", SCOPE_REQUEST))
                .map(bean -> (RequestScopeBean) bean)
                .orElseThrow();
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
