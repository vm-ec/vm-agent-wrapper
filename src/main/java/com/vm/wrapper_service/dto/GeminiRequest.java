package com.vm.wrapper_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    private Contents contents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contents {
        private List<Part> parts;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Part {
            private String text;
        }
    }

    public static GeminiRequest create(String input) {
        Contents contents = new Contents(List.of(new Contents.Part(input)));
        return new GeminiRequest(contents);
    }
}
