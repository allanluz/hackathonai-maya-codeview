package com.sinqia.maya.service;

import com.sinqia.maya.entity.AuxiliaryFile;
import com.sinqia.maya.entity.ConfigurationSettings;
import com.sinqia.maya.entity.FileAnalysis;
import com.sinqia.maya.repository.AuxiliaryFileRepository;
import com.sinqia.maya.repository.ConfigurationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servi�o de integra��o com Sinqia AI (LLM).
 * 
 * Respons�vel por:
 * - Enviar c�digo para an�lise via LLM
 * - Processar respostas de IA
 * - Gerenciar prompts e configura��es
 * - Criar relat�rios executivos
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SinqiaAiService {

    private final RestTemplate restTemplate;
    private final ConfigurationSettingsRepository configRepository;
    private final AuxiliaryFileRepository auxiliaryFileRepository;

    @Value("${maya.ai.endpoint}")
    private String aiEndpoint;

    @Value("${maya.ai.api-key}")
    private String apiKey;

    @Value("${maya.ai.model:gpt-4}")
    private String defaultModel;

    @Value("${maya.ai.timeout:30000}")
    private int timeoutMs;

    /**
     * Analisar c�digo usando LLM
     */
    public AiAnalysisResult analyzeCode(String filePath, String content, String language) {
        log.info("Iniciando an�lise de IA para arquivo: {}", filePath);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Buscar prompt apropriado
            String analysisPrompt = getAnalysisPrompt(language, filePath);
            
            // Montar request para LLM
            AiRequest request = buildAnalysisRequest(analysisPrompt, content, filePath);
            
            // Chamar API
            AiResponse response = callAiApi(request);
            
            if (response != null && response.isSuccess()) {
                long duration = System.currentTimeMillis() - startTime;
                
                log.info("An�lise de IA conclu�da em {}ms para arquivo: {}", duration, filePath);
                
                return new AiAnalysisResult(
                        true,
                        response.content(),
                        response.confidence(),
                        extractIssues(response.content()),
                        extractSuggestions(response.content()),
                        duration
                );
            }
            
        } catch (Exception e) {
            log.error("Erro na an�lise de IA para arquivo {}: {}", filePath, e.getMessage());
        }
        
        long duration = System.currentTimeMillis() - startTime;
        return new AiAnalysisResult(false, "Erro na an�lise", 0.0, Collections.emptyList(), 
                                   Collections.emptyList(), duration);
    }

    /**
     * Gerar relat�rio executivo baseado em an�lise de c�digo
     */
    public String generateExecutiveReport(List<FileAnalysis> analyses, String projectName, String commitSha) {
        log.info("Gerando relat�rio executivo para projeto: {} commit: {}", projectName, commitSha);
        
        try {
            // Buscar template de relat�rio
            String reportTemplate = getExecutiveReportTemplate();
            
            // Preparar dados agregados
            Map<String, Object> reportData = aggregateAnalysisData(analyses, projectName, commitSha);
            
            // Montar prompt para LLM
            String prompt = buildExecutiveReportPrompt(reportTemplate, reportData);
            
            AiRequest request = new AiRequest(
                    defaultModel,
                    prompt,
                    0.3, // Temperatura baixa para relat�rios
                    2000, // Tokens m�ximos
                    Map.of("type", "executive_report")
            );
            
            AiResponse response = callAiApi(request);
            
            if (response != null && response.isSuccess()) {
                log.info("Relat�rio executivo gerado com sucesso");
                return response.content();
            }
            
        } catch (Exception e) {
            log.error("Erro ao gerar relat�rio executivo: {}", e.getMessage());
        }
        
        // Fallback: relat�rio simples
        return generateFallbackExecutiveReport(analyses, projectName, commitSha);
    }

    /**
     * Sugerir melhorias baseadas em padr�es Sinqia
     */
    public List<String> suggestImprovements(String code, String className, String packageName) {
        log.debug("Sugerindo melhorias para classe: {}", className);
        
        try {
            String improvementPrompt = getImprovementPrompt();
            
            String prompt = String.format("""
                %s
                
                C�DIGO PARA AN�LISE:
                Classe: %s
                Pacote: %s
                
                ```java
                %s
                ```
                
                Forne�a 3-5 sugest�es espec�ficas de melhoria seguindo os padr�es Sinqia.
                """, improvementPrompt, className, packageName, code);
            
            AiRequest request = new AiRequest(
                    defaultModel,
                    prompt,
                    0.5,
                    1000,
                    Map.of("type", "improvements")
            );
            
            AiResponse response = callAiApi(request);
            
            if (response != null && response.isSuccess()) {
                return parseImprovementSuggestions(response.content());
            }
            
        } catch (Exception e) {
            log.error("Erro ao sugerir melhorias: {}", e.getMessage());
        }
        
        return getDefaultImprovements();
    }

    /**
     * Verificar se c�digo segue padr�es Sinqia
     */
    public SinqiaComplianceResult checkSinqiaCompliance(String code, String filePath) {
        log.debug("Verificando conformidade Sinqia para arquivo: {}", filePath);
        
        try {
            String compliancePrompt = getSinqiaCompliancePrompt();
            
            String prompt = String.format("""
                %s
                
                ARQUIVO: %s
                
                ```java
                %s
                ```
                
                Analise a conformidade com os padr�es Sinqia e retorne um JSON com:
                - score (0-100)
                - violations (lista de viola��es)
                - recommendations (recomenda��es)
                """, compliancePrompt, filePath, code);
            
            AiRequest request = new AiRequest(
                    defaultModel,
                    prompt,
                    0.2,
                    1500,
                    Map.of("type", "compliance_check")
            );
            
            AiResponse response = callAiApi(request);
            
            if (response != null && response.isSuccess()) {
                return parseComplianceResult(response.content());
            }
            
        } catch (Exception e) {
            log.error("Erro na verifica��o de conformidade: {}", e.getMessage());
        }
        
        // Fallback
        return new SinqiaComplianceResult(75, Collections.emptyList(), getDefaultRecommendations());
    }

    // M�todos privados de suporte
    
    private AiResponse callAiApi(AiRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(aiEndpoint, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseAiResponse(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro na chamada da API de IA: {}", e.getMessage());
        }
        
        return null;
    }
    
    private String getAnalysisPrompt(String language, String filePath) {
        // Buscar prompt espec�fico do banco
        Optional<AuxiliaryFile> promptFile = auxiliaryFileRepository
                .findByTypeAndName("prompt", "analysis_" + language);
        
        if (promptFile.isPresent()) {
            return promptFile.get().getContent();
        }
        
        // Prompt padr�o
        return String.format("""
            Voc� � um especialista em an�lise de c�digo Java com foco nos padr�es de desenvolvimento Sinqia.
            
            Analise o c�digo fornecido considerando:
            1. Vazamentos de conex�o (empresta/devolve)
            2. Complexidade ciclom�tica
            3. Padr�es arquiteturais Sinqia
            4. Seguran�a
            5. Performance
            
            Arquivo: %s
            Linguagem: %s
            
            Forne�a uma an�lise detalhada em formato markdown com:
            - Resumo executivo
            - Issues encontrados (cr�ticos, errors, warnings)
            - Sugest�es de melhoria
            - Score de qualidade (0-100)
            """, filePath, language);
    }
    
    private String getExecutiveReportTemplate() {
        Optional<AuxiliaryFile> template = auxiliaryFileRepository
                .findByTypeAndName("template", "executive_report");
        
        return template.map(AuxiliaryFile::getContent)
                      .orElse(getDefaultExecutiveTemplate());
    }
    
    private String getImprovementPrompt() {
        Optional<AuxiliaryFile> prompt = auxiliaryFileRepository
                .findByTypeAndName("prompt", "improvements");
        
        return prompt.map(AuxiliaryFile::getContent)
                    .orElse("Analise o c�digo e sugira melhorias seguindo os padr�es Sinqia.");
    }
    
    private String getSinqiaCompliancePrompt() {
        Optional<AuxiliaryFile> prompt = auxiliaryFileRepository
                .findByTypeAndName("prompt", "sinqia_compliance");
        
        return prompt.map(AuxiliaryFile::getContent)
                    .orElse("Verifique se o c�digo segue os padr�es de desenvolvimento Sinqia.");
    }
    
    private AiRequest buildAnalysisRequest(String prompt, String content, String filePath) {
        String fullPrompt = String.format("""
            %s
            
            C�DIGO PARA AN�LISE:
            
            ```java
            %s
            ```
            """, prompt, content);
        
        return new AiRequest(
                defaultModel,
                fullPrompt,
                0.3,
                2000,
                Map.of(
                        "file_path", filePath,
                        "analysis_type", "code_review"
                )
        );
    }
    
    private Map<String, Object> aggregateAnalysisData(List<FileAnalysis> analyses, String projectName, String commitSha) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("project_name", projectName);
        data.put("commit_sha", commitSha);
        data.put("total_files", analyses.size());
        
        // Agrega��es
        double averageScore = analyses.stream()
                .mapToDouble(FileAnalysis::getScore)
                .average()
                .orElse(0.0);
        
        int totalIssues = analyses.stream()
                .mapToInt(a -> a.getIssues().size())
                .sum();
        
        long criticalIssues = analyses.stream()
                .flatMap(a -> a.getIssues().stream())
                .filter(i -> i.getSeverity().name().equals("CRITICAL"))
                .count();
        
        data.put("average_score", averageScore);
        data.put("total_issues", totalIssues);
        data.put("critical_issues", criticalIssues);
        
        return data;
    }
    
    private String buildExecutiveReportPrompt(String template, Map<String, Object> data) {
        return String.format("""
            Gere um relat�rio executivo baseado no template e dados fornecidos.
            
            TEMPLATE:
            %s
            
            DADOS:
            %s
            
            Crie um relat�rio profissional e conciso para apresenta��o executiva.
            """, template, data.toString());
    }
    
    private AiResponse parseAiResponse(Map<String, Object> responseData) {
        try {
            // Adaptar para diferentes formatos de resposta da API
            String content = "";
            Double confidence = 0.8;
            
            if (responseData.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseData.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    content = (String) message.get("content");
                }
            } else if (responseData.containsKey("content")) {
                content = (String) responseData.get("content");
            }
            
            return new AiResponse(true, content, confidence, null);
            
        } catch (Exception e) {
            log.error("Erro ao fazer parse da resposta da IA: {}", e.getMessage());
            return new AiResponse(false, "Erro no parse", 0.0, e.getMessage());
        }
    }
    
    private List<String> extractIssues(String aiResponse) {
        // Parse simples de issues do texto da IA
        List<String> issues = new ArrayList<>();
        
        String[] lines = aiResponse.split("\\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("issue") || 
                line.toLowerCase().contains("problema") ||
                line.toLowerCase().contains("erro")) {
                issues.add(line.trim());
            }
        }
        
        return issues;
    }
    
    private List<String> extractSuggestions(String aiResponse) {
        List<String> suggestions = new ArrayList<>();
        
        String[] lines = aiResponse.split("\\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("sugest") || 
                line.toLowerCase().contains("recomend") ||
                line.toLowerCase().contains("melhoria")) {
                suggestions.add(line.trim());
            }
        }
        
        return suggestions;
    }
    
    private List<String> parseImprovementSuggestions(String content) {
        // Parse das sugest�es de melhoria
        List<String> suggestions = new ArrayList<>();
        
        String[] lines = content.split("\\n");
        for (String line : lines) {
            if (line.trim().startsWith("-") || line.trim().startsWith("*")) {
                suggestions.add(line.trim().substring(1).trim());
            }
        }
        
        return suggestions.isEmpty() ? getDefaultImprovements() : suggestions;
    }
    
    private SinqiaComplianceResult parseComplianceResult(String content) {
        try {
            // Tentar extrair informa��es do texto
            // Em implementa��o real, usar parser JSON adequado
            
            int score = 75; // Default
            List<String> violations = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();
            
            // Parse simples
            if (content.toLowerCase().contains("conformidade alta")) {
                score = 85;
            } else if (content.toLowerCase().contains("conformidade baixa")) {
                score = 50;
            }
            
            return new SinqiaComplianceResult(score, violations, recommendations);
            
        } catch (Exception e) {
            log.error("Erro ao fazer parse do resultado de conformidade: {}", e.getMessage());
            return new SinqiaComplianceResult(75, Collections.emptyList(), getDefaultRecommendations());
        }
    }
    
    private String getDefaultExecutiveTemplate() {
        return """
            # Relat�rio Executivo - An�lise MAYA
            
            ## Resumo Executivo
            - Projeto: {{project_name}}
            - Commit: {{commit_sha}}
            - Arquivos analisados: {{total_files}}
            - Score m�dio: {{average_score}}
            
            ## Indicadores de Qualidade
            - Issues cr�ticos: {{critical_issues}}
            - Total de issues: {{total_issues}}
            
            ## Recomenda��es
            - Foco na corre��o de vazamentos de conex�o
            - Melhoria da documenta��o
            - Implementa��o de testes unit�rios
            """;
    }
    
    private List<String> getDefaultImprovements() {
        return List.of(
                "Adicionar documenta��o Javadoc nas classes e m�todos p�blicos",
                "Implementar tratamento adequado de exce��es",
                "Seguir padr�es de nomenclatura Sinqia",
                "Adicionar valida��es de entrada",
                "Implementar testes unit�rios"
        );
    }
    
    private List<String> getDefaultRecommendations() {
        return List.of(
                "Revisar estrutura de pacotes",
                "Aplicar padr�es de codifica��o Sinqia",
                "Melhorar tratamento de erros",
                "Adicionar logs apropriados"
        );
    }
    
    private String generateFallbackExecutiveReport(List<FileAnalysis> analyses, String projectName, String commitSha) {
        StringBuilder report = new StringBuilder();
        
        report.append("# Relat�rio Executivo - An�lise MAYA\n\n");
        report.append("## Resumo Executivo\n\n");
        report.append("- **Projeto:** ").append(projectName).append("\n");
        report.append("- **Commit:** ").append(commitSha).append("\n");
        report.append("- **Arquivos analisados:** ").append(analyses.size()).append("\n\n");
        
        double avgScore = analyses.stream().mapToDouble(FileAnalysis::getScore).average().orElse(0.0);
        report.append("- **Score m�dio:** ").append(String.format("%.1f", avgScore)).append("/100\n\n");
        
        long criticalIssues = analyses.stream()
                .flatMap(a -> a.getIssues().stream())
                .filter(i -> i.getSeverity().name().equals("CRITICAL"))
                .count();
        
        report.append("## Indicadores de Qualidade\n\n");
        report.append("- **Issues cr�ticos:** ").append(criticalIssues).append("\n");
        
        int totalIssues = analyses.stream().mapToInt(a -> a.getIssues().size()).sum();
        report.append("- **Total de issues:** ").append(totalIssues).append("\n\n");
        
        report.append("## Recomenda��es\n\n");
        report.append("- Foco na corre��o de vazamentos de conex�o\n");
        report.append("- Melhoria da documenta��o t�cnica\n");
        report.append("- Implementa��o de testes unit�rios\n");
        report.append("- Revis�o de complexidade de m�todos\n");
        
        return report.toString();
    }
    
    // Records para DTOs
    
    public record AiRequest(
            String model,
            String prompt,
            double temperature,
            int maxTokens,
            Map<String, Object> metadata
    ) {}
    
    public record AiResponse(
            boolean success,
            String content,
            double confidence,
            String error
    ) {
        public boolean isSuccess() {
            return success;
        }
    }
    
    public record AiAnalysisResult(
            boolean success,
            String content,
            double confidence,
            List<String> issues,
            List<String> suggestions,
            long processingTimeMs
    ) {}
    
    public record SinqiaComplianceResult(
            int score,
            List<String> violations,
            List<String> recommendations
    ) {}
}
