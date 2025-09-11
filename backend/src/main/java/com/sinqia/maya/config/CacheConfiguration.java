package com.sinqia.maya.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache para o sistema MAYA.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(
                java.util.Arrays.asList("configurations", "codeReviews", "fileAnalyses")
        );
        return cacheManager;
    }
}
