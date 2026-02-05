package com.vm.wrapper_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIResponse {
    private List<Choice> choices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Message message;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String content;
        }
    }

    public String extractText() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        Choice choice = choices.get(0);
        if (choice.getMessage() == null) {
            return "";
        }
        return choice.getMessage().getContent();
    }
}
