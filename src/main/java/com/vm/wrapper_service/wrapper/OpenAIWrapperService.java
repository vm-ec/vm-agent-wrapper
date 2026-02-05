package com.vm.wrapper_service.wrapper;

import com.vm.wrapper_service.dto.OpenAIRequest;
import com.vm.wrapper_service.dto.OpenAIResponse;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenAIWrapperService {

    @Autowired
    private RestClient restClient;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.endpoint}")
    private String apiEndpoint;

    @Value("${openai.api.model}")
    private String model;

    public String callOpenAI(String input) {
        try {
            String validation = validateKeys();
            if(!StringUtils.isBlank(validation)) {
                return validation;
            }

            String enhancedInput = "You are an intelligent and helpful AI assistant.\n" +
                    "Respond naturally, clearly, and professionally.\n" +
                    "Structure the answer using simple numbered points or bullet points where appropriate.\n" +
                    "Keep the response concise and focused on the question.\n" +
                    "Adjust the level of detail to match the complexity of the question.\n" +
                    "Avoid unnecessary formatting symbols.\n\n" +
                    "Answer the user query below.\n" + input;

            OpenAIRequest request = OpenAIRequest.create(enhancedInput, model);

            OpenAIResponse response = restClient
                    .post()
                    .uri(apiEndpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(OpenAIResponse.class);

            if (response == null) return "ERROR: No response received from OpenAI API";

            return response.extractText();

        } catch (Exception e) {
            return "ERROR: Failed to call OpenAI API: " + e.getMessage();
        }
    }

    private String validateKeys() {
        if (apiKey == null || apiKey.isEmpty()) {
            return "ERROR: OpenAI API key is not configured";
        }
        return "";
    }
}
