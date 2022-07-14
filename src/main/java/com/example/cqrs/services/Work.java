package com.example.cqrs.services;

import com.example.cqrs.dto.WorkResultDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class Work {

    private Long delay;
    private UUID id;
    private LocalDateTime evictTime;
    private Long ttl;
    private WorkResultDto workResult;

    @Builder.Default
    private RequestStatus requestStatus = RequestStatus.PROCESS;
}
