package com.vm.wrapper_service.wrapper;

import com.vm.wrapper_service.dto.GeminiRequest;
import com.vm.wrapper_service.dto.GeminiResponse;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiWrapperService {

    @Autowired
    private RestClient restClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.endpoint}")
    private String apiEndpoint;

    @Value("${gemini.api.model}")
    private String model;

    public String callGemini(String input) {
        try {
            String validation = validateKeys();
            if(!StringUtils.isBlank(validation)) {
                return validation;
            }

            // Prompt Engineering: Prepend instructions
            String enhancedInput = "You are an intelligent and helpful AI assistant.\n" +
                    "\n" +
                    "Respond naturally, clearly, and professionally.\n" +
                    "Structure the answer using simple numbered points or bullet points where appropriate.\n" +
                    "Keep the response concise and focused on the question.\n" +
                    "Adjust the level of detail to match the complexity of the question.\n" +
                    "Avoid unnecessary formatting symbols.\n" +
                    "\n" +
                    "Answer the user query below.\n" + input;

            GeminiRequest request = GeminiRequest.create(enhancedInput);

            String uri = apiEndpoint + "/models/" + model + ":generateContent?key=" + apiKey;

            GeminiResponse response = restClient
                    .post()
                    .uri(uri)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

           if (response == null) return "ERROR: No response received from Gemini API";

            // Return raw text
            return response.extractText();

        } catch (Exception e) {
            return "ERROR: Failed to call Gemini API: " + e.getMessage();
        }
    }

    private String validateKeys() {
        return "";
    }


}