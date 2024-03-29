package com.example.cqrs.services;

import com.example.cqrs.dto.WorkDto;
import com.example.cqrs.dto.WorkResultDto;
import com.example.cqrs.other.OtherServiceImpl;
import com.example.cqrs.other.RequestScopeBean;
import com.example.cqrs.other.RequestScopeBeanImpl;
import com.example.cqrs.utils.Parameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.cqrs.services.RequestStatus.NOT_FOUND;
import static java.lang.Runtime.getRuntime;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Service
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    public static final int NUM_OF_WORKERS = getRuntime().availableProcessors() + 1;
    public static final InheritableThreadLocal<LocalData> threadContext = new InheritableThreadLocal<>();

    private final Parameters parameters;
    private final OtherServiceImpl otherService;
    private final RequestScopeBean requestScopeBean;

    private final Map<UUID, Work> requestIdToWorkMap;
    private final ExecutorService executor;


    public ProcessServiceImpl(Parameters parameters,
                              OtherServiceImpl otherService,
                              RequestScopeBeanImpl requestScopeBean) {
        this.parameters = parameters;
        this.otherService = otherService;
        this.requestScopeBean = requestScopeBean;

        this.requestIdToWorkMap = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(NUM_OF_WORKERS);
    }

    @Scheduled(fixedDelay = 2000L)
    public void evict() {
        evictResults();
    }

    @Override
    public RequestStatus getRequestStatus(UUID requestId) {
        return ofNullable(requestIdToWorkMap.get(requestId))
                .map(Work::getRequestStatus)
                .orElse(NOT_FOUND);
    }

    @Override
    public UUID addWork(WorkDto dto) {
        return addWorkToExecutorQueue(dto);
    }

    @Override
    public WorkResultDto getResult(UUID requestId) {
        Work work = ofNullable(requestIdToWorkMap.get(requestId))
                .orElseThrow(() -> new RuntimeException("Неизвестный requestId"));

        return ofNullable(work.getWorkResult())
                .orElseThrow(() -> new RuntimeException("Результат не готов"));
    }

    @Override
    public void evictResults() {
        requestIdToWorkMap.values().stream()
                .filter(work -> nonNull(work.getEvictTime()))
                .filter(work -> now().isAfter(work.getEvictTime()))
                .map(Work::getId)
                .forEach(requestIdToWorkMap.keySet()::remove);
    }

    @Override
    public Integer getCacheSize() {
        return requestIdToWorkMap.size();
    }

    private UUID addWorkToExecutorQueue(WorkDto dto) {
        requestScopeBean.setStr(dto.getDelay());

        LocalData localData = new LocalData();
        threadContext.set(localData);
        log.info("[{}] локальные данные для потока = {}", Thread.currentThread().getId(), localData.getUuid());

        UUID requestId = UUID.randomUUID();
        Work work = Work.builder()
                .id(requestId)
                .delay(dto.getDelay())
                .ttl(parameters.getProcessingTtlSec())
                .build();
        requestIdToWorkMap.put(requestId, work);
        executor.submit(new Worker(requestIdToWorkMap, work, otherService, requestScopeBean));
        return requestId;
    }
}
