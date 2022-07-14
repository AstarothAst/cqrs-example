package com.example.cqrs.services;

import com.example.cqrs.dto.WorkDto;
import com.example.cqrs.dto.WorkResultDto;

import java.util.UUID;

public interface ProcessService {

    RequestStatus getRequestStatus(UUID requestId);

    UUID addWork(WorkDto dto);

    WorkResultDto getResult(UUID requestId);

    void evictResults();

    Integer getCacheSize();
}
