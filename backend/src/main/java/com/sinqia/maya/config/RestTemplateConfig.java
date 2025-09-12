package com.sinqia.maya.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuração do RestTemplate para chamadas HTTP.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Configura o RestTemplate com timeouts apropriados
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }
}
