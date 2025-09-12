package com.sinqia.maya.service;

import com.sinqia.maya.entity.ConfigurationSettings;
import com.sinqia.maya.repository.ConfigurationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de gerenciamento de configurações do sistema MAYA.
 * 
 * Responsável por:
 * - Gerenciar configurações de sistema
 * - Cache de configurações
 * - Validação de configurações
 * - Auditoria de mudanças
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConfigurationService {

    private final ConfigurationSettingsRepository configRepository;

    /**
     * Buscar configuração por chave (com cache)
     */
    @Cacheable(value = "configurations", key = "#configKey")
    public Optional<ConfigurationSettings> getConfiguration(String configKey) {
        log.debug("Buscando configuração: {}", configKey);
        return configRepository.findByKeyName(configKey);
    }

    /**
     * Obter valor de configuração como String
     */
    public String getConfigValue(String configKey, String defaultValue) {
        Optional<ConfigurationSettings> config = getConfiguration(configKey);
        return config.map(ConfigurationSettings::getConfigValue).orElse(defaultValue);
    }

    /**
     * Obter valor de configuração como Integer
     */
    public Integer getConfigValueAsInt(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("Valor de configuração inválido para {}: {}", configKey, value);
            }
        }
        return defaultValue;
    }

    /**
     * Obter valor de configuração como Boolean
     */
    public Boolean getConfigValueAsBool(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Obter valor de configuração como Double
     */
    public Double getConfigValueAsDouble(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                log.warn("Valor de configuração inválido para {}: {}", configKey, value);
            }
        }
        return defaultValue;
    }

    /**
     * Salvar ou atualizar configuração
     */
    @CacheEvict(value = "configurations", key = "#configKey")
    public ConfigurationSettings saveConfiguration(String configKey, String configValue, String description) {
        log.info("Salvando configuração: {} = {}", configKey, configValue);
        
        Optional<ConfigurationSettings> existing = configRepository.findByKeyName(configKey);
        
        ConfigurationSettings config;
        if (existing.isPresent()) {
            config = existing.get();
            config.setConfigValue(configValue);
            config.setDescription(description);
            config.setUpdatedAt(LocalDateTime.now());
        } else {
            config = new ConfigurationSettings();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setDescription(description);
            config.setCategory("SYSTEM");
            config.setIsActive(true);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
        }
        
        return configRepository.save(config);
    }

    /**
     * Listar configurações por categoria
     */
    public List<ConfigurationSettings> getConfigurationsByCategory(String category) {
        log.debug("Buscando configurações da categoria: {}", category);
        return configRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Listar todas as configurações ativas
     */
    public List<ConfigurationSettings> getAllActiveConfigurations() {
        return configRepository.findByIsActiveTrueOrderByCategory();
    }

    /**
     * Desativar configuração
     */
    @CacheEvict(value = "configurations", key = "#configKey")
    public void deactivateConfiguration(String configKey) {
        log.info("Desativando configuração: {}", configKey);
        
        Optional<ConfigurationSettings> config = configRepository.findByKeyName(configKey);
        if (config.isPresent()) {
            ConfigurationSettings setting = config.get();
            setting.setIsActive(false);
            setting.setUpdatedAt(LocalDateTime.now());
            configRepository.save(setting);
        }
    }

    /**
     * Limpar cache de configurações
     */
    @CacheEvict(value = "configurations", allEntries = true)
    public void clearConfigurationCache() {
        log.info("Cache de configurações limpo");
    }

    /**
     * Obter configurações de TFS
     */
    public TfsConfiguration getTfsConfiguration() {
        return new TfsConfiguration(
                getConfigValue("tfs.base.url", "https://tfs.sinqia.com.br"),
                getConfigValue("tfs.organization", "sinqia"),
                getConfigValue("tfs.personal.access.token", ""),
                getConfigValue("tfs.api.version", "7.0"),
                getConfigValueAsInt("tfs.timeout.ms", 30000),
                getConfigValueAsBool("tfs.auto.analysis.enabled", true)
        );
    }

    /**
     * Obter configurações de IA
     */
    public AiConfiguration getAiConfiguration() {
        return new AiConfiguration(
                getConfigValue("ai.endpoint", "https://api.openai.com/v1/chat/completions"),
                getConfigValue("ai.api.key", ""),
                getConfigValue("ai.model", "gpt-4"),
                getConfigValueAsDouble("ai.temperature", 0.3),
                getConfigValueAsInt("ai.max.tokens", 2000),
                getConfigValueAsInt("ai.timeout.ms", 30000),
                getConfigValueAsBool("ai.enabled", true)
        );
    }

    /**
     * Obter configurações de análise MAYA
     */
    public MayaAnalysisConfiguration getMayaConfiguration() {
        return new MayaAnalysisConfiguration(
                getConfigValueAsInt("maya.complexity.threshold", 15),
                getConfigValueAsInt("maya.connection.imbalance.threshold", 3),
                getConfigValueAsInt("maya.min.score.threshold", 70),
                getConfigValueAsBool("maya.auto.fix.enabled", false),
                getConfigValueAsBool("maya.executive.report.enabled", true),
                getConfigValueAsInt("maya.analysis.timeout.ms", 120000)
        );
    }

    /**
     * Atualizar configurações de TFS
     */
    public void updateTfsConfiguration(TfsConfiguration config) {
        log.info("Atualizando configurações de TFS");
        
        saveConfiguration("tfs.base.url", config.baseUrl(), "URL base do Azure DevOps/TFS");
        saveConfiguration("tfs.organization", config.organization(), "Organização no Azure DevOps");
        saveConfiguration("tfs.personal.access.token", config.personalAccessToken(), "Token de acesso pessoal");
        saveConfiguration("tfs.api.version", config.apiVersion(), "Versão da API do Azure DevOps");
        saveConfiguration("tfs.timeout.ms", config.timeoutMs().toString(), "Timeout para chamadas TFS em ms");
        saveConfiguration("tfs.auto.analysis.enabled", config.autoAnalysisEnabled().toString(), "Análise automática habilitada");
    }

    /**
     * Atualizar configurações de IA
     */
    public void updateAiConfiguration(AiConfiguration config) {
        log.info("Atualizando configurações de IA");
        
        saveConfiguration("ai.endpoint", config.endpoint(), "Endpoint da API de IA");
        saveConfiguration("ai.api.key", config.apiKey(), "Chave de API para serviço de IA");
        saveConfiguration("ai.model", config.model(), "Modelo de IA a ser usado");
        saveConfiguration("ai.temperature", config.temperature().toString(), "Temperatura para geração de texto");
        saveConfiguration("ai.max.tokens", config.maxTokens().toString(), "Número máximo de tokens");
        saveConfiguration("ai.timeout.ms", config.timeoutMs().toString(), "Timeout para chamadas IA em ms");
        saveConfiguration("ai.enabled", config.enabled().toString(), "Serviço de IA habilitado");
    }

    /**
     * Atualizar configurações de análise MAYA
     */
    public void updateMayaConfiguration(MayaAnalysisConfiguration config) {
        log.info("Atualizando configurações de análise MAYA");
        
        saveConfiguration("maya.complexity.threshold", config.complexityThreshold().toString(), 
                        "Limite de complexidade ciclomática");
        saveConfiguration("maya.connection.imbalance.threshold", config.connectionImbalanceThreshold().toString(), 
                        "Limite de desequilíbrio de conexões");
        saveConfiguration("maya.min.score.threshold", config.minScoreThreshold().toString(), 
                        "Score mínimo aceitável");
        saveConfiguration("maya.auto.fix.enabled", config.autoFixEnabled().toString(), 
                        "Correção automática habilitada");
        saveConfiguration("maya.executive.report.enabled", config.executiveReportEnabled().toString(), 
                        "Relatório executivo habilitado");
        saveConfiguration("maya.analysis.timeout.ms", config.analysisTimeoutMs().toString(), 
                        "Timeout para análise em ms");
    }

    /**
     * Validar configurações do sistema
     */
    public ConfigurationValidationResult validateSystemConfiguration() {
        log.info("Validando configurações do sistema");
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validar TFS
        TfsConfiguration tfsConfig = getTfsConfiguration();
        if (tfsConfig.baseUrl().isEmpty()) {
            errors.add("URL base do TFS não configurada");
        }
        if (tfsConfig.personalAccessToken().isEmpty()) {
            warnings.add("Token de acesso do TFS não configurado");
        }
        
        // Validar IA
        AiConfiguration aiConfig = getAiConfiguration();
        if (aiConfig.enabled() && aiConfig.apiKey().isEmpty()) {
            errors.add("Chave da API de IA não configurada");
        }
        if (aiConfig.endpoint().isEmpty()) {
            errors.add("Endpoint da IA não configurado");
        }
        
        // Validar MAYA
        MayaAnalysisConfiguration mayaConfig = getMayaConfiguration();
        if (mayaConfig.complexityThreshold() <= 0) {
            errors.add("Limite de complexidade deve ser maior que 0");
        }
        if (mayaConfig.analysisTimeoutMs() < 30000) {
            warnings.add("Timeout de análise muito baixo (recomendado: >= 30s)");
        }
        
        boolean isValid = errors.isEmpty();
        
        log.info("Validação de configurações: {} - {} erros, {} avisos", 
                isValid ? "SUCESSO" : "FALHA", errors.size(), warnings.size());
        
        return new ConfigurationValidationResult(isValid, errors, warnings);
    }

    /**
     * Restaurar configurações padrão
     */
    public void restoreDefaultConfigurations() {
        log.info("Restaurando configurações padrão");
        
        // TFS defaults
        saveConfiguration("tfs.base.url", "https://tfs.sinqia.com.br", "URL base do Azure DevOps/TFS");
        saveConfiguration("tfs.organization", "sinqia", "Organização no Azure DevOps");
        saveConfiguration("tfs.api.version", "7.0", "Versão da API do Azure DevOps");
        saveConfiguration("tfs.timeout.ms", "30000", "Timeout para chamadas TFS em ms");
        saveConfiguration("tfs.auto.analysis.enabled", "true", "Análise automática habilitada");
        
        // AI defaults
        saveConfiguration("ai.endpoint", "https://api.openai.com/v1/chat/completions", "Endpoint da API de IA");
        saveConfiguration("ai.model", "gpt-4", "Modelo de IA a ser usado");
        saveConfiguration("ai.temperature", "0.3", "Temperatura para geração de texto");
        saveConfiguration("ai.max.tokens", "2000", "Número máximo de tokens");
        saveConfiguration("ai.timeout.ms", "30000", "Timeout para chamadas IA em ms");
        saveConfiguration("ai.enabled", "true", "Serviço de IA habilitado");
        
        // MAYA defaults
        saveConfiguration("maya.complexity.threshold", "15", "Limite de complexidade ciclomática");
        saveConfiguration("maya.connection.imbalance.threshold", "3", "Limite de desequilíbrio de conexões");
        saveConfiguration("maya.min.score.threshold", "70", "Score mínimo aceitável");
        saveConfiguration("maya.auto.fix.enabled", "false", "Correção automática habilitada");
        saveConfiguration("maya.executive.report.enabled", "true", "Relatório executivo habilitado");
        saveConfiguration("maya.analysis.timeout.ms", "120000", "Timeout para análise em ms");
        
        clearConfigurationCache();
    }

    // Records para DTOs de configuração
    
    public record TfsConfiguration(
            String baseUrl,
            String organization,
            String personalAccessToken,
            String apiVersion,
            Integer timeoutMs,
            Boolean autoAnalysisEnabled
    ) {}
    
    public record AiConfiguration(
            String endpoint,
            String apiKey,
            String model,
            Double temperature,
            Integer maxTokens,
            Integer timeoutMs,
            Boolean enabled
    ) {}
    
    public record MayaAnalysisConfiguration(
            Integer complexityThreshold,
            Integer connectionImbalanceThreshold,
            Integer minScoreThreshold,
            Boolean autoFixEnabled,
            Boolean executiveReportEnabled,
            Integer analysisTimeoutMs
    ) {}
    
    public record ConfigurationValidationResult(
            boolean isValid,
            List<String> errors,
            List<String> warnings
    ) {}
}
