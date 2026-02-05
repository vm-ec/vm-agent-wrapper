package com.vm.wrapper_service.metrics;

import com.vm.wrapper_service.dto.AgentRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class AiAgentMetrics {

    private final MeterRegistry registry;

    // Keep last used model (for WrapperService compatibility)
    private volatile String currentModel = "gemini";

    // Provider based meters
    private final Map<String, Counter> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> tokensInCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> tokensOutCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> latencyTimers = new ConcurrentHashMap<>();

    public AiAgentMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    // -------- INTERNAL HELPERS --------

    private Counter getCounter(Map<String, Counter> map, String provider, String name) {
        return map.computeIfAbsent(provider, p ->
                Counter.builder(name)
                        .tag("provider", p)
                        .register(registry));
    }

    private Timer getTimer(String provider) {
        return latencyTimers.computeIfAbsent(provider, p ->
                Timer.builder("ai_agent_latency_seconds")
                        .publishPercentiles(0.95)
                        .publishPercentileHistogram()
                        .tag("provider", p)
                        .register(registry));
    }

    // -------- PUBLIC API (USED BY WrapperService) --------

    public void incrementRequest() {
        getCounter(requestCounters, currentModel, "ai_agent_requests_total").increment();
    }

    public void incrementError() {
        getCounter(errorCounters, currentModel, "ai_agent_errors_total").increment();
    }

    public void addTokensIn(long count) {
        getCounter(tokensInCounters, currentModel, "ai_agent_tokens_in_total").increment(count);
    }

    public void addTokensOut(long count) {
        getCounter(tokensOutCounters, currentModel, "ai_agent_tokens_out_total").increment(count);
    }

    public <T> T recordLatency(Supplier<T> supplier) {
        return getTimer(currentModel).record(supplier);
    }

    // -------- METRICS READ --------

    public double getRequestCount() {
        return requestCounters.values().stream().mapToDouble(Counter::count).sum();
    }

    public double getErrorCount() {
        return errorCounters.values().stream().mapToDouble(Counter::count).sum();
    }

    public double getTokensInCount() {
        return tokensInCounters.values().stream().mapToDouble(Counter::count).sum();
    }

    public double getTokensOutCount() {
        return tokensOutCounters.values().stream().mapToDouble(Counter::count).sum();
    }

    public double getP95Latency() {
        return latencyTimers.values().stream()
                .findFirst()
                .map(t -> t.takeSnapshot().percentileValues()[0].value())
                .orElse(0.0);
    }


    // -------- MODEL TRACKING --------

    public void capturePreCallDetails(String modelName, AgentRequest request) {
        this.currentModel = modelName;
    }

    public void capturePostCallDetails(String modelName, AgentRequest request, String aiOutput) {
        // reserved for future
    }

    public String getModelName() {
        return currentModel;
    }

    // -------- PROVIDER-SPECIFIC METRICS --------

    public double getRequestCountByProvider(String provider) {
        return requestCounters.getOrDefault(provider, Counter.builder("dummy").register(registry)).count();
    }

    public double getErrorCountByProvider(String provider) {
        return errorCounters.getOrDefault(provider, Counter.builder("dummy").register(registry)).count();
    }

    public double getTokensInCountByProvider(String provider) {
        return tokensInCounters.getOrDefault(provider, Counter.builder("dummy").register(registry)).count();
    }

    public double getTokensOutCountByProvider(String provider) {
        return tokensOutCounters.getOrDefault(provider, Counter.builder("dummy").register(registry)).count();
    }

    public double getP95LatencyByProvider(String provider) {
        Timer timer = latencyTimers.get(provider);
        if (timer == null) return 0.0;
        return timer.takeSnapshot().percentileValues()[0].value();
    }
}
