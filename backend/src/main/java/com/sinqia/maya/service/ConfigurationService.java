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
 * Servi�o de gerenciamento de configura��es do sistema MAYA.
 * 
 * Respons�vel por:
 * - Gerenciar configura��es de sistema
 * - Cache de configura��es
 * - Valida��o de configura��es
 * - Auditoria de mudan�as
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
     * Buscar configura��o por chave (com cache)
     */
    @Cacheable(value = "configurations", key = "#configKey")
    public Optional<ConfigurationSettings> getConfiguration(String configKey) {
        log.debug("Buscando configura��o: {}", configKey);
        return configRepository.findByKeyName(configKey);
    }

    /**
     * Obter valor de configura��o como String
     */
    public String getConfigValue(String configKey, String defaultValue) {
        Optional<ConfigurationSettings> config = getConfiguration(configKey);
        return config.map(ConfigurationSettings::getConfigValue).orElse(defaultValue);
    }

    /**
     * Obter valor de configura��o como Integer
     */
    public Integer getConfigValueAsInt(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("Valor de configura��o inv�lido para {}: {}", configKey, value);
            }
        }
        return defaultValue;
    }

    /**
     * Obter valor de configura��o como Boolean
     */
    public Boolean getConfigValueAsBool(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Obter valor de configura��o como Double
     */
    public Double getConfigValueAsDouble(String configKey, Double defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                log.warn("Valor de configura��o inv�lido para {}: {}", configKey, value);
            }
        }
        return defaultValue;
    }

    /**
     * Salvar ou atualizar configura��o
     */
    @CacheEvict(value = "configurations", key = "#configKey")
    public ConfigurationSettings saveConfiguration(String configKey, String configValue, String description) {
        log.info("Salvando configura��o: {} = {}", configKey, configValue);
        
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
     * Listar configura��es por categoria
     */
    public List<ConfigurationSettings> getConfigurationsByCategory(String category) {
        log.debug("Buscando configura��es da categoria: {}", category);
        return configRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Listar todas as configura��es ativas
     */
    public List<ConfigurationSettings> getAllActiveConfigurations() {
        return configRepository.findByIsActiveTrueOrderByCategory();
    }

    /**
     * Desativar configura��o
     */
    @CacheEvict(value = "configurations", key = "#configKey")
    public void deactivateConfiguration(String configKey) {
        log.info("Desativando configura��o: {}", configKey);
        
        Optional<ConfigurationSettings> config = configRepository.findByKeyName(configKey);
        if (config.isPresent()) {
            ConfigurationSettings setting = config.get();
            setting.setIsActive(false);
            setting.setUpdatedAt(LocalDateTime.now());
            configRepository.save(setting);
        }
    }

    /**
     * Limpar cache de configura��es
     */
    @CacheEvict(value = "configurations", allEntries = true)
    public void clearConfigurationCache() {
        log.info("Cache de configura��es limpo");
    }

    /**
     * Obter configura��es de TFS
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
     * Obter configura��es de IA
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
     * Obter configura��es de an�lise MAYA
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
     * Atualizar configura��es de TFS
     */
    public void updateTfsConfiguration(TfsConfiguration config) {
        log.info("Atualizando configura��es de TFS");
        
        saveConfiguration("tfs.base.url", config.baseUrl(), "URL base do Azure DevOps/TFS");
        saveConfiguration("tfs.organization", config.organization(), "Organiza��o no Azure DevOps");
        saveConfiguration("tfs.personal.access.token", config.personalAccessToken(), "Token de acesso pessoal");
        saveConfiguration("tfs.api.version", config.apiVersion(), "Vers�o da API do Azure DevOps");
        saveConfiguration("tfs.timeout.ms", config.timeoutMs().toString(), "Timeout para chamadas TFS em ms");
        saveConfiguration("tfs.auto.analysis.enabled", config.autoAnalysisEnabled().toString(), "An�lise autom�tica habilitada");
    }

    /**
     * Atualizar configura��es de IA
     */
    public void updateAiConfiguration(AiConfiguration config) {
        log.info("Atualizando configura��es de IA");
        
        saveConfiguration("ai.endpoint", config.endpoint(), "Endpoint da API de IA");
        saveConfiguration("ai.api.key", config.apiKey(), "Chave de API para servi�o de IA");
        saveConfiguration("ai.model", config.model(), "Modelo de IA a ser usado");
        saveConfiguration("ai.temperature", config.temperature().toString(), "Temperatura para gera��o de texto");
        saveConfiguration("ai.max.tokens", config.maxTokens().toString(), "N�mero m�ximo de tokens");
        saveConfiguration("ai.timeout.ms", config.timeoutMs().toString(), "Timeout para chamadas IA em ms");
        saveConfiguration("ai.enabled", config.enabled().toString(), "Servi�o de IA habilitado");
    }

    /**
     * Atualizar configura��es de an�lise MAYA
     */
    public void updateMayaConfiguration(MayaAnalysisConfiguration config) {
        log.info("Atualizando configura��es de an�lise MAYA");
        
        saveConfiguration("maya.complexity.threshold", config.complexityThreshold().toString(), 
                        "Limite de complexidade ciclom�tica");
        saveConfiguration("maya.connection.imbalance.threshold", config.connectionImbalanceThreshold().toString(), 
                        "Limite de desequil�brio de conex�es");
        saveConfiguration("maya.min.score.threshold", config.minScoreThreshold().toString(), 
                        "Score m�nimo aceit�vel");
        saveConfiguration("maya.auto.fix.enabled", config.autoFixEnabled().toString(), 
                        "Corre��o autom�tica habilitada");
        saveConfiguration("maya.executive.report.enabled", config.executiveReportEnabled().toString(), 
                        "Relat�rio executivo habilitado");
        saveConfiguration("maya.analysis.timeout.ms", config.analysisTimeoutMs().toString(), 
                        "Timeout para an�lise em ms");
    }

    /**
     * Validar configura��es do sistema
     */
    public ConfigurationValidationResult validateSystemConfiguration() {
        log.info("Validando configura��es do sistema");
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Validar TFS
        TfsConfiguration tfsConfig = getTfsConfiguration();
        if (tfsConfig.baseUrl().isEmpty()) {
            errors.add("URL base do TFS n�o configurada");
        }
        if (tfsConfig.personalAccessToken().isEmpty()) {
            warnings.add("Token de acesso do TFS n�o configurado");
        }
        
        // Validar IA
        AiConfiguration aiConfig = getAiConfiguration();
        if (aiConfig.enabled() && aiConfig.apiKey().isEmpty()) {
            errors.add("Chave da API de IA n�o configurada");
        }
        if (aiConfig.endpoint().isEmpty()) {
            errors.add("Endpoint da IA n�o configurado");
        }
        
        // Validar MAYA
        MayaAnalysisConfiguration mayaConfig = getMayaConfiguration();
        if (mayaConfig.complexityThreshold() <= 0) {
            errors.add("Limite de complexidade deve ser maior que 0");
        }
        if (mayaConfig.analysisTimeoutMs() < 30000) {
            warnings.add("Timeout de an�lise muito baixo (recomendado: >= 30s)");
        }
        
        boolean isValid = errors.isEmpty();
        
        log.info("Valida��o de configura��es: {} - {} erros, {} avisos", 
                isValid ? "SUCESSO" : "FALHA", errors.size(), warnings.size());
        
        return new ConfigurationValidationResult(isValid, errors, warnings);
    }

    /**
     * Restaurar configura��es padr�o
     */
    public void restoreDefaultConfigurations() {
        log.info("Restaurando configura��es padr�o");
        
        // TFS defaults
        saveConfiguration("tfs.base.url", "https://tfs.sinqia.com.br", "URL base do Azure DevOps/TFS");
        saveConfiguration("tfs.organization", "sinqia", "Organiza��o no Azure DevOps");
        saveConfiguration("tfs.api.version", "7.0", "Vers�o da API do Azure DevOps");
        saveConfiguration("tfs.timeout.ms", "30000", "Timeout para chamadas TFS em ms");
        saveConfiguration("tfs.auto.analysis.enabled", "true", "An�lise autom�tica habilitada");
        
        // AI defaults
        saveConfiguration("ai.endpoint", "https://api.openai.com/v1/chat/completions", "Endpoint da API de IA");
        saveConfiguration("ai.model", "gpt-4", "Modelo de IA a ser usado");
        saveConfiguration("ai.temperature", "0.3", "Temperatura para gera��o de texto");
        saveConfiguration("ai.max.tokens", "2000", "N�mero m�ximo de tokens");
        saveConfiguration("ai.timeout.ms", "30000", "Timeout para chamadas IA em ms");
        saveConfiguration("ai.enabled", "true", "Servi�o de IA habilitado");
        
        // MAYA defaults
        saveConfiguration("maya.complexity.threshold", "15", "Limite de complexidade ciclom�tica");
        saveConfiguration("maya.connection.imbalance.threshold", "3", "Limite de desequil�brio de conex�es");
        saveConfiguration("maya.min.score.threshold", "70", "Score m�nimo aceit�vel");
        saveConfiguration("maya.auto.fix.enabled", "false", "Corre��o autom�tica habilitada");
        saveConfiguration("maya.executive.report.enabled", "true", "Relat�rio executivo habilitado");
        saveConfiguration("maya.analysis.timeout.ms", "120000", "Timeout para an�lise em ms");
        
        clearConfigurationCache();
    }

    // Records para DTOs de configura��o
    
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
