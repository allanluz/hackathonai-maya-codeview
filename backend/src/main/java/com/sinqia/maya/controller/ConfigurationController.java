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
 * Controller REST para gerenciamento de configura��es do sistema.
 * 
 * Endpoints para:
 * - Gerenciar configura��es de sistema
 * - Configura��es de TFS/Azure DevOps  
 * - Configura��es de IA
 * - Configura��es de an�lise MAYA
 * - Valida��o e restore de configura��es
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
     * Listar todas as configura��es ativas
     */
    @GetMapping
    public ResponseEntity<List<ConfigurationSettings>> getAllConfigurations() {
        log.debug("Listando todas as configura��es ativas");
        
        List<ConfigurationSettings> configurations = configurationService.getAllActiveConfigurations();
        return ResponseEntity.ok(configurations);
    }

    /**
     * Listar configura��es por categoria
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ConfigurationSettings>> getConfigurationsByCategory(@PathVariable String category) {
        log.debug("Listando configura��es da categoria: {}", category);
        
        List<ConfigurationSettings> configurations = configurationService.getConfigurationsByCategory(category);
        return ResponseEntity.ok(configurations);
    }

    /**
     * Obter configura��o espec�fica por chave
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<ConfigurationSettings> getConfiguration(@PathVariable String configKey) {
        log.debug("Buscando configura��o: {}", configKey);
        
        return configurationService.getConfiguration(configKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Salvar ou atualizar configura��o
     */
    @PostMapping
    public ResponseEntity<ConfigurationSettings> saveConfiguration(@RequestBody ConfigurationRequest request) {
        log.info("Salvando configura��o: {} = {}", request.configKey(), request.configValue());
        
        try {
            ConfigurationSettings config = configurationService.saveConfiguration(
                    request.configKey(),
                    request.configValue(),
                    request.description()
            );
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("Erro ao salvar configura��o {}: {}", request.configKey(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Desativar configura��o
     */
    @DeleteMapping("/{configKey}")
    public ResponseEntity<Void> deactivateConfiguration(@PathVariable String configKey) {
        log.info("Desativando configura��o: {}", configKey);
        
        try {
            configurationService.deactivateConfiguration(configKey);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Erro ao desativar configura��o {}: {}", configKey, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obter configura��es de TFS
     */
    @GetMapping("/tfs")
    public ResponseEntity<TfsConfiguration> getTfsConfiguration() {
        log.debug("Obtendo configura��es de TFS");
        
        TfsConfiguration config = configurationService.getTfsConfiguration();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualizar configura��es de TFS
     */
    @PutMapping("/tfs")
    public ResponseEntity<OperationResponse> updateTfsConfiguration(@RequestBody TfsConfiguration config) {
        log.info("Atualizando configura��es de TFS");
        
        try {
            configurationService.updateTfsConfiguration(config);
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configura��es de TFS atualizadas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao atualizar configura��es de TFS: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Obter configura��es de IA
     */
    @GetMapping("/ai")
    public ResponseEntity<AiConfiguration> getAiConfiguration() {
        log.debug("Obtendo configura��es de IA");
        
        AiConfiguration config = configurationService.getAiConfiguration();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualizar configura��es de IA
     */
    @PutMapping("/ai")
    public ResponseEntity<OperationResponse> updateAiConfiguration(@RequestBody AiConfiguration config) {
        log.info("Atualizando configura��es de IA");
        
        try {
            configurationService.updateAiConfiguration(config);
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configura��es de IA atualizadas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao atualizar configura��es de IA: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Obter configura��es de an�lise MAYA
     */
    @GetMapping("/maya")
    public ResponseEntity<MayaAnalysisConfiguration> getMayaConfiguration() {
        log.debug("Obtendo configura��es de an�lise MAYA");
        
        MayaAnalysisConfiguration config = configurationService.getMayaConfiguration();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualizar configura��es de an�lise MAYA
     */
    @PutMapping("/maya")
    public ResponseEntity<OperationResponse> updateMayaConfiguration(@RequestBody MayaAnalysisConfiguration config) {
        log.info("Atualizando configura��es de an�lise MAYA");
        
        try {
            configurationService.updateMayaConfiguration(config);
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configura��es de an�lise MAYA atualizadas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao atualizar configura��es MAYA: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Validar configura��es do sistema
     */
    @PostMapping("/validate")
    public ResponseEntity<ConfigurationValidationResult> validateConfigurations() {
        log.info("Validando configura��es do sistema");
        
        try {
            ConfigurationValidationResult result = configurationService.validateSystemConfiguration();
            
            if (result.isValid()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("Erro durante valida��o de configura��es: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new ConfigurationValidationResult(
                            false,
                            List.of("Erro interno durante valida��o: " + e.getMessage()),
                            List.of()
                    ));
        }
    }

    /**
     * Restaurar configura��es padr�o
     */
    @PostMapping("/restore-defaults")
    public ResponseEntity<OperationResponse> restoreDefaultConfigurations() {
        log.info("Restaurando configura��es padr�o");
        
        try {
            configurationService.restoreDefaultConfigurations();
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Configura��es padr�o restauradas com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao restaurar configura��es padr�o: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Limpar cache de configura��es
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<OperationResponse> clearConfigurationCache() {
        log.info("Limpando cache de configura��es");
        
        try {
            configurationService.clearConfigurationCache();
            
            return ResponseEntity.ok(new OperationResponse(
                    true,
                    "Cache de configura��es limpo com sucesso"
            ));
            
        } catch (Exception e) {
            log.error("Erro ao limpar cache: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new OperationResponse(false, "Erro: " + e.getMessage()));
        }
    }

    /**
     * Testar configura��es de conectividade
     */
    @PostMapping("/test-connectivity")
    public ResponseEntity<ConnectivityTestResponse> testConnectivity() {
        log.info("Testando conectividade com servi�os externos");
        
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
     * Exportar configura��es atuais
     */
    @GetMapping("/export")
    public ResponseEntity<ConfigurationExport> exportConfigurations() {
        log.info("Exportando configura��es atuais");
        
        try {
            List<ConfigurationSettings> allConfigs = configurationService.getAllActiveConfigurations();
            
            return ResponseEntity.ok(new ConfigurationExport(
                    allConfigs,
                    allConfigs.size(),
                    System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao exportar configura��es: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // M�todos privados de suporte
    
    private ConnectivityTestResult testTfsConnectivity() {
        try {
            TfsConfiguration config = configurationService.getTfsConfiguration();
            
            if (config.baseUrl().isEmpty()) {
                return new ConnectivityTestResult(false, "URL base do TFS n�o configurada");
            }
            
            if (config.personalAccessToken().isEmpty()) {
                return new ConnectivityTestResult(false, "Token de acesso n�o configurado");
            }
            
            // Em implementa��o real, testar conex�o efetiva
            return new ConnectivityTestResult(true, "Configura��o TFS OK");
            
        } catch (Exception e) {
            return new ConnectivityTestResult(false, "Erro: " + e.getMessage());
        }
    }
    
    private ConnectivityTestResult testAiConnectivity() {
        try {
            AiConfiguration config = configurationService.getAiConfiguration();
            
            if (!config.enabled()) {
                return new ConnectivityTestResult(false, "Servi�o de IA desabilitado");
            }
            
            if (config.endpoint().isEmpty()) {
                return new ConnectivityTestResult(false, "Endpoint da IA n�o configurado");
            }
            
            if (config.apiKey().isEmpty()) {
                return new ConnectivityTestResult(false, "Chave da API n�o configurada");
            }
            
            // Em implementa��o real, testar conex�o efetiva
            return new ConnectivityTestResult(true, "Configura��o IA OK");
            
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
