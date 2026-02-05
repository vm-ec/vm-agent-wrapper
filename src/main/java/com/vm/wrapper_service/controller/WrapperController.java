package com.vm.wrapper_service.controller;

import com.vm.wrapper_service.dto.AgentRequest;
import com.vm.wrapper_service.dto.AgentResponse;
import com.vm.wrapper_service.service.WrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class WrapperController {

    private final WrapperService wrapperService;

    @Autowired
    public WrapperController(WrapperService wrapperService) {
        this.wrapperService = wrapperService;
    }

    @PostMapping("/process")
    public AgentResponse processInput(@RequestBody AgentRequest request) {
        return wrapperService.handleRequest(request);
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody AgentRequest request) {
        return wrapperService.handleChatRequest(request);
    }

    @GetMapping("/metrics")
    public AgentResponse getMetrics() {
        return wrapperService.getMetrics();
    }
}
