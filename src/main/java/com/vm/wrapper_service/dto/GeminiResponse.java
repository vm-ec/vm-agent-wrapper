package com.vm.wrapper_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Content content;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Content {
            private List<Part> parts;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Part {
                private String text;
            }
        }
    }

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null || candidate.getContent().getParts().isEmpty()) {
            return "";
        }
        return candidate.getContent().getParts().get(0).getText();
    }
}
