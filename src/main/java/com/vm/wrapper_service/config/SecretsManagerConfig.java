package com.vm.wrapper_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import jakarta.annotation.PostConstruct;

import static com.vm.wrapper_service.utils.Constants.GEMINI_API_KEY;
import static com.vm.wrapper_service.utils.Constants.OPENAI_API_KEY;

@Configuration
@ConditionalOnProperty(name = "aws.secretsmanager.enabled", havingValue = "true", matchIfMissing = true)
public class SecretsManagerConfig {

    @Value("${aws.secretsmanager.secret-name}")
    private String secretName;

    @Value("${aws.secretsmanager.region}")
    private String region;

    @PostConstruct
    public void loadSecrets() {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build()) {

            GetSecretValueResponse response = client.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId(secretName)
                            .build()
            );

            String secret = response.secretString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode secretJson = mapper.readTree(secret);

            if (secretJson.has(GEMINI_API_KEY)) {
                System.setProperty(GEMINI_API_KEY, secretJson.get(GEMINI_API_KEY).asText());
            }
            if (secretJson.has(OPENAI_API_KEY)) {
                System.setProperty(OPENAI_API_KEY, secretJson.get(OPENAI_API_KEY).asText());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load secrets from AWS Secrets Manager", e);
        }
    }
}
