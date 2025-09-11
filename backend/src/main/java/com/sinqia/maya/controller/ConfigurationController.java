package com.sinqia.maya.controller;

import com.sinqia.maya.service.ConfigurationService;
import com.sinqia.maya.service.ConfigurationService.*;
import com.sinqia.maya.entity.ConfigurationSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de configurações do sistema.
 * 
 * Endpoints para:
 * - Gerenciar configurações de sistema
 * - Configurações de TFS/Azure DevOps  
 * - Configurações de IA
 * - Configurações de análise MAYA
 * - Validação e restore de configurações
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ConfigurationController {

    private final ConfigurationService configurationService;

    /**
     * Listar todas as configurações ativas
     */
    @GetMapping
    public ResponseEntity<List<ConfigurationSettings>> getAllConfigurations() {
        log.debug("Listando todas as configurações ativas");
        
        List<ConfigurationSettings> configurations = configurationService.getAllActiveConfigurations();
        return ResponseEntity.ok(configurations);
    }

    /**
     * Listar configurações por categoria
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ConfigurationSettings>> getConfigurationsByCategory(@PathVariable String category) {
        log.debug("Listando configurações da categoria: {}", category);
        
        List<ConfigurationSettings> configurations = configurationService.getConfigurationsByCategory(category);
        return ResponseEntity.ok(configurations);
    }

    /**
     * Obter configuração específica por chave
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<ConfigurationSettings> getConfiguration(@PathVariable String configKey) {
        log.debug("Buscando configuração: {}", configKey);
        
        return configurationService.getConfiguration(configKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Salvar ou atualizar configuração
     */
    @PostMapping
    public ResponseEntity<ConfigurationSettings> saveConfiguration(@RequestBody ConfigurationRequest request) {
        log.info("Salvando configuração: {} = {}", request.configKey(), request.configValue());
        
        try {
            ConfigurationSettings config = configurationService.saveConfiguration(
                    request.configKey(),
                    request.configValue(),
                    request.description()
            );
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("Erro ao salvar configuração {}: {}", request.configKey(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Desativar configuração
     */
    @DeleteMapping("/{configKey}")
    public ResponseEntity<Void> deactivateConfiguration(@PathVariable String configKey) {
        log.info("Desativando configuração: {}", configKey);
        
        try {
            configurationService.deactivateConfiguration(configKey);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Erro ao desativar configuração {}: {}", configKey, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obter configurações de TFS
     */
    @GetMapping("/tfs")
    public ResponseEntity<TfsConfiguration> getTfsConfiguration() {
        log.debug("Obtendo configurações de TFS");
        
        TfsConfiguration config = configurationService.getTfsConfiguration();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualizar configurações de TFS
     */
    @PutMapping("/tfs")
    public ResponseEntity<OperationResponse> updateTfsConfiguration(@RequestBody TfsConfiguration config) {
        log.info("Atualizando configurações de TFS");
        
        try {
            configurationService.updateTfsConfiguration(config);
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configurações de TFS atualizadas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao atualizar configurações de TFS: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Obter configurações de IA
     */
    @GetMapping("/ai")
    public ResponseEntity<AiConfiguration> getAiConfiguration() {
        log.debug("Obtendo configurações de IA");
        
        AiConfiguration config = configurationService.getAiConfiguration();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualizar configurações de IA
     */
    @PutMapping("/ai")
    public ResponseEntity<OperationResponse> updateAiConfiguration(@RequestBody AiConfiguration config) {
        log.info("Atualizando configurações de IA");
        
        try {
            configurationService.updateAiConfiguration(config);
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configurações de IA atualizadas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao atualizar configurações de IA: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Obter configurações de análise MAYA
     */
    @GetMapping("/maya")
    public ResponseEntity<MayaAnalysisConfiguration> getMayaConfiguration() {
        log.debug("Obtendo configurações de análise MAYA");
        
        MayaAnalysisConfiguration config = configurationService.getMayaConfiguration();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualizar configurações de análise MAYA
     */
    @PutMapping("/maya")
    public ResponseEntity<OperationResponse> updateMayaConfiguration(@RequestBody MayaAnalysisConfiguration config) {
        log.info("Atualizando configurações de análise MAYA");
        
        try {
            configurationService.updateMayaConfiguration(config);
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configurações de análise MAYA atualizadas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao atualizar configurações MAYA: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Validar configurações do sistema
     */
    @PostMapping("/validate")
    public ResponseEntity<ConfigurationValidationResult> validateConfigurations() {
        log.info("Validando configurações do sistema");
        
        try {
            ConfigurationValidationResult result = configurationService.validateSystemConfiguration();
            
            if (result.isValid()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("Erro durante validação de configurações: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new ConfigurationValidationResult(
                            false,
                            List.of("Erro interno durante validação: " + e.getMessage()),
                            List.of()
                    ));
        }
    }

    /**
     * Restaurar configurações padrão
     */
    @PostMapping("/restore-defaults")
    public ResponseEntity<OperationResponse> restoreDefaultConfigurations() {
        log.info("Restaurando configurações padrão");
        
        try {
            configurationService.restoreDefaultConfigurations();
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configurações padrão restauradas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao restaurar configurações padrão: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Limpar cache de configurações
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<OperationResponse> clearConfigurationCache() {
        log.info("Limpando cache de configurações");
        
        try {
            configurationService.clearConfigurationCache();
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Cache de configurações limpo com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao limpar cache: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Testar configurações de conectividade
     */
    @PostMapping("/test-connectivity")
    public ResponseEntity<ConnectivityTestResponse> testConnectivity() {
        log.info("Testando conectividade com serviços externos");
        
        ConnectivityTestResult tfsTest = testTfsConnectivity();
        ConnectivityTestResult aiTest = testAiConnectivity();
        
        boolean allConnected = tfsTest.connected() && aiTest.connected();
        
        return ResponseEntity.ok(new ConnectivityTestResponse(
                allConnected,
                tfsTest,
                aiTest,
                System.currentTimeMillis()
        ));
    }

    /**
     * Exportar configurações atuais
     */
    @GetMapping("/export")
    public ResponseEntity<ConfigurationExport> exportConfigurations() {
        log.info("Exportando configurações atuais");
        
        try {
            List<ConfigurationSettings> allConfigs = configurationService.getAllActiveConfigurations();
            
            return ResponseEntity.ok(new ConfigurationExport(
                    allConfigs,
                    allConfigs.size(),
                    System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao exportar configurações: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Métodos privados de suporte
    
    private ConnectivityTestResult testTfsConnectivity() {
        try {
            TfsConfiguration config = configurationService.getTfsConfiguration();
            
            if (config.baseUrl().isEmpty()) {
                return new ConnectivityTestResult(false, "URL base do TFS não configurada");
            }
            
            if (config.personalAccessToken().isEmpty()) {
                return new ConnectivityTestResult(false, "Token de acesso não configurado");
            }
            
            // Em implementação real, testar conexão efetiva
            return new ConnectivityTestResult(true, "Configuração TFS OK");
            
        } catch (Exception e) {
            return new ConnectivityTestResult(false, "Erro: " + e.getMessage());
        }
    }
    
    private ConnectivityTestResult testAiConnectivity() {
        try {
            AiConfiguration config = configurationService.getAiConfiguration();
            
            if (!config.enabled()) {
                return new ConnectivityTestResult(false, "Serviço de IA desabilitado");
            }
            
            if (config.endpoint().isEmpty()) {
                return new ConnectivityTestResult(false, "Endpoint da IA não configurado");
            }
            
            if (config.apiKey().isEmpty()) {
                return new ConnectivityTestResult(false, "Chave da API não configurada");
            }
            
            // Em implementação real, testar conexão efetiva
            return new ConnectivityTestResult(true, "Configuração IA OK");
            
        } catch (Exception e) {
            return new ConnectivityTestResult(false, "Erro: " + e.getMessage());
        }
    }

    // DTOs para requests e responses
    
    public record ConfigurationRequest(
            String configKey,
            String configValue,
            String description
    ) {}
    
    public record OperationResponse(
            boolean success,
            String message
    ) {}
    
    public record ConnectivityTestResult(
            boolean connected,
            String message
    ) {}
    
    public record ConnectivityTestResponse(
            boolean allConnected,
            ConnectivityTestResult tfsTest,
            ConnectivityTestResult aiTest,
            long timestamp
    ) {}
    
    public record ConfigurationExport(
            List<ConfigurationSettings> configurations,
            int totalCount,
            long exportTimestamp
    ) {}
}
