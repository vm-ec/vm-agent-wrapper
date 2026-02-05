package com.vm.wrapper_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    public class JacksonConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}