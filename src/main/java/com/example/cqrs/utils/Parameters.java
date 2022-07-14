package com.example.cqrs.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Parameters {

    @Value("${processing.ttl:120}")
    private Long processingTtlSec;
}
