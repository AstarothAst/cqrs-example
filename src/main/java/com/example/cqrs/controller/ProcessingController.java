package com.example.cqrs.controller;

import com.example.cqrs.dto.WorkDto;
import com.example.cqrs.dto.WorkResultDto;
import com.example.cqrs.services.ProcessService;
import com.example.cqrs.services.RequestStatus;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@ResponseBody
public class ProcessingController {

    private final ProcessService processService;

    @GetMapping("/status")
    public RequestStatus getRequestStatus(@RequestParam UUID requestId) {
        return processService.getRequestStatus(requestId);
    }

    @PostMapping("/add-work")
    public UUID addWork(@RequestBody WorkDto dto) {
        return processService.addWork(dto);
    }

    @GetMapping("/get")
    public WorkResultDto getResult(@RequestParam UUID requestId){
        return processService.getResult(requestId);
    }

    @GetMapping("/cache-size")
    public Integer getCacheSize(){
        return processService.getCacheSize();
    }
}
