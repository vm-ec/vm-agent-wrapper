package com.vm.wrapper_service.service;

import com.vm.wrapper_service.dto.AgentRequest;
import com.vm.wrapper_service.dto.AgentResponse;
import com.vm.wrapper_service.metrics.AiAgentMetrics;
import com.vm.wrapper_service.wrapper.GeminiWrapperService;
import com.vm.wrapper_service.wrapper.OpenAIWrapperService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WrapperService {

    private final GeminiWrapperService geminiWrapperService;
    private final OpenAIWrapperService openAIWrapperService;
    private final AiAgentMetrics metrics;

    @Value("${gemini.api.model}")
    private String geminiModelName;
    @Value("${openai.api.model}")
    private String openAiModelName;

    public WrapperService(GeminiWrapperService geminiWrapperService,
                          OpenAIWrapperService openAIWrapperService,
                          AiAgentMetrics metrics) {
        this.geminiWrapperService = geminiWrapperService;
        this.openAIWrapperService = openAIWrapperService;
        this.metrics = metrics;
    }

    public AgentResponse handleRequest(AgentRequest request) {

        // ---- Metrics: request received ----
        metrics.incrementRequest();

        // ---- Tokens IN (simple approximation) ----
        long tokensIn = request.getInput() != null ? request.getInput().length() : 0;
        metrics.addTokensIn(tokensIn);

        // ---- Call LLM and measure latency ----
        long startTime = System.currentTimeMillis();

        String aiOutput;
        try {
            aiOutput = geminiWrapperService.callGemini(request.getInput());
        } catch (Exception e) {
            metrics.incrementError();
            throw e;
        }

        long endTime = System.currentTimeMillis();
        long latencyMs = endTime - startTime;

        // ---- Tokens OUT (simple approximation) ----
        long tokensOut = aiOutput != null ? aiOutput.length() : 0;
        metrics.addTokensOut(tokensOut);

        // ---- Build MATRIX output ----
        List<String> columns = List.of("parameter", "type", "value");
        List<Object> rows = new ArrayList<>();

        rows.add(new String[]{"LLM Model", "string", getModelName(request)});
        rows.add(List.of("Requests Total", "counter", metrics.getRequestCount()));
        rows.add(List.of("Errors Total", "counter", metrics.getErrorCount()));
        rows.add(List.of("Tokens In", "counter", tokensIn));
        rows.add(List.of("Tokens Out", "counter", tokensOut));
        rows.add(List.of("Latency (ms)", "timer", latencyMs));

        Map<String, Object> matrix = new HashMap<>();
        matrix.put("columns", columns);
        matrix.put("rows", rows);

        // ---- Response ----
        AgentResponse response = new AgentResponse();
        response.setOutput(matrix);

        return response;
    }

    public Map<String, String> handleChatRequest(AgentRequest request) {
        // Track metrics
        metrics.incrementRequest();
        long tokensIn = request.getInput() != null ? request.getInput().length() : 0;
        metrics.addTokensIn(tokensIn);

        String modelType = request.getModel() != null ? request.getModel() : "gemini";
        String aiOutput = "";

        metrics.capturePreCallDetails(modelType, request);
        if(modelType.equals("gemini")) {
            aiOutput = geminiWrapperService.callGemini(request.getInput());
        } else if(modelType.equals("openai")) {
            aiOutput = openAIWrapperService.callOpenAI(request.getInput());
        }
        metrics.capturePostCallDetails(modelType, request, aiOutput);

        long tokensOut = aiOutput != null ? aiOutput.length() : 0;
        metrics.addTokensOut(tokensOut);

        Map<String, String> response = new HashMap<>();
        response.put("output", aiOutput);
        return response;
    }

    public AgentResponse getMetrics() {
        List<String> columns = List.of("parameter", "type", "value");
        List<Map<String, Object>> rows = new ArrayList<>();

        // ---------- GEMINI ----------
        Map<String, Object> geminiRow = new LinkedHashMap<>();
        geminiRow.put("model", "gemini");
        geminiRow.put("requestsTotal", metrics.getRequestCountByProvider("gemini"));
        geminiRow.put("errorsTotal", metrics.getErrorCountByProvider("gemini"));
        geminiRow.put("tokensInTotal", metrics.getTokensInCountByProvider("gemini"));
        geminiRow.put("tokensOutTotal", metrics.getTokensOutCountByProvider("gemini"));
        geminiRow.put("latencyP95Seconds", metrics.getP95LatencyByProvider("gemini"));
        rows.add(geminiRow);

        // ---------- OPENAI ----------
        Map<String, Object> openaiRow = new LinkedHashMap<>();
        openaiRow.put("model", "openai");
        openaiRow.put("requestsTotal", metrics.getRequestCountByProvider("openai"));
        openaiRow.put("errorsTotal", metrics.getErrorCountByProvider("openai"));
        openaiRow.put("tokensInTotal", metrics.getTokensInCountByProvider("openai"));
        openaiRow.put("tokensOutTotal", metrics.getTokensOutCountByProvider("openai"));
        openaiRow.put("latencyP95Seconds", metrics.getP95LatencyByProvider("openai"));
        rows.add(openaiRow);

        Map<String, Object> matrix = new HashMap<>();
        matrix.put("columns", columns);
        matrix.put("rows", rows);

        AgentResponse response = new AgentResponse();
        response.setOutput(matrix);
        return response;
    }


    private String getModelName(AgentRequest request) {
        String modelType = request.getModel() != null ? request.getModel() : "gemini";
        String modelName = "";
        if(modelType.equals("gemini")) {
            modelName = geminiModelName;
        } else if(modelType.equals("openai")) {
            modelName = openAiModelName;
        }
        return modelName;
    }
}
