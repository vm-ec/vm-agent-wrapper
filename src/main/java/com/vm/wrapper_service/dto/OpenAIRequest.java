package com.vm.wrapper_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {
    private String model;
    private List<Message> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    public static OpenAIRequest create(String input, String model) {
        Message message = new Message("user", input);
        OpenAIRequest request = new OpenAIRequest();
        request.setModel(model);
        request.setMessages(List.of(message));
        return request;
    }
}
