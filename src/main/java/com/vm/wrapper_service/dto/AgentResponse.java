package com.vm.wrapper_service.dto;


import lombok.Data;

import java.util.Map;

@Data
public class AgentResponse {
    // Change from String to Object or Map<String,Object> to hold JSON object
    private Map<String, Object> output;

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }
}

