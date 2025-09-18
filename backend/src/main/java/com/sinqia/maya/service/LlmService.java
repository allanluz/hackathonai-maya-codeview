package com.sinqia.maya.service;

import com.sinqia.maya.dto.LlmModelDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Servi�o para integra��o com modelos LLM (Large Language Models)
 * Suporta integra��o com Gemini (Google), GPT (OpenAI), etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${maya.llm.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${maya.llm.openai.api-key:}")
    private String openaiApiKey;

    @Value("${maya.llm.enabled:true}")
    private boolean llmEnabled;

    @Value("${maya.llm.timeout:30000}")
    private int timeoutMs;

    // URLs das APIs
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1";

    private final Map<String, LlmModelDto> modelCache = new HashMap<>();

    @PostConstruct
    public void initialize() {
        log.info("Inicializando LlmService - LLM habilitado: {}", llmEnabled);
        if (llmEnabled) {
            initializeModels();
        }
    }

    /**
     * Inicializa cache de modelos dispon�veis
     */
    private void initializeModels() {
        // Modelos Gemini dispon�veis
        if (isGeminiConfigured()) {
            modelCache.put("gemini-2.0-flash-001", LlmModelDto.builder()
                    .id("gemini-2.0-flash-001")
                    .name("Gemini 2.0 Flash")
                    .family("Google")
                    .description("Modelo avan�ado da Google para an�lise de c�digo e gera��o de texto")
                    .version("2.0")
                    .maxTokens(32768)
                    .available(true)
                    .type("multimodal")
                    .avgResponseTime(2000)
                    .build());

            modelCache.put("gemini-pro", LlmModelDto.builder()
                    .id("gemini-pro")
                    .name("Gemini Pro")
                    .family("Google")
                    .description("Modelo profissional da Google para tarefas complexas")
                    .version("1.0")
                    .maxTokens(30720)
                    .available(true)
                    .type("text")
                    .avgResponseTime(3000)
                    .build());
        }

        // Modelos OpenAI dispon�veis
        if (isOpenAiConfigured()) {
            modelCache.put("gpt-4-turbo", LlmModelDto.builder()
                    .id("gpt-4-turbo")
                    .name("GPT-4 Turbo")
                    .family("OpenAI")
                    .description("Modelo GPT-4 otimizado para performance e custo")
                    .version("turbo")
                    .maxTokens(128000)
                    .available(true)
                    .type("text")
                    .avgResponseTime(4000)
                    .build());

            modelCache.put("gpt-3.5-turbo", LlmModelDto.builder()
                    .id("gpt-3.5-turbo")
                    .name("GPT-3.5 Turbo")
                    .family("OpenAI")
                    .description("Modelo GPT-3.5 r�pido e eficiente")
                    .version("turbo")
                    .maxTokens(4096)
                    .available(true)
                    .type("text")
                    .avgResponseTime(2500)
                    .build());
        }

        log.info("Inicializados {} modelos LLM", modelCache.size());
    }

    /**
     * Obt�m lista de modelos LLM dispon�veis
     */
    public List<String> getAvailableModels() {
        if (!llmEnabled) {
            return getFallbackModels();
        }

        List<String> availableModels = new ArrayList<>();
        
        for (LlmModelDto model : modelCache.values()) {
            if (model.getAvailable()) {
                availableModels.add(model.getId());
            }
        }

        // Se n�o h� modelos configurados, retorna fallback
        if (availableModels.isEmpty()) {
            return getFallbackModels();
        }

        return availableModels;
    }

    /**
     * Modelos fallback para desenvolvimento/demonstra��o
     */
    private List<String> getFallbackModels() {
        return Arrays.asList(
            "gemini-2.0-flash-001",
            "gemini-pro",
            "gpt-4-turbo",
            "gpt-3.5-turbo"
        );
    }

    /**
     * Analisa c�digo usando modelo LLM especificado
     */
    public String analyzeCode(String code, String fileName, String model) {
        log.info("Analisando c�digo com modelo: {} para arquivo: {}", model, fileName);

        if (!llmEnabled) {
            return generateMockAnalysis(code, fileName, model);
        }

        try {
            String prompt = buildAnalysisPrompt(code, fileName);
            return callLlmApi(model, prompt);
        } catch (Exception e) {
            log.error("Erro ao analisar c�digo com LLM", e);
            return generateMockAnalysis(code, fileName, model);
        }
    }

    /**
     * Gera sugest�es de melhoria para o c�digo
     */
    public String generateSuggestions(String code, String fileName, String model) {
        log.info("Gerando sugest�es com modelo: {} para arquivo: {}", model, fileName);

        if (!llmEnabled) {
            return generateMockSuggestions(code, fileName);
        }

        try {
            String prompt = buildSuggestionsPrompt(code, fileName);
            return callLlmApi(model, prompt);
        } catch (Exception e) {
            log.error("Erro ao gerar sugest�es com LLM", e);
            return generateMockSuggestions(code, fileName);
        }
    }

    /**
     * Realiza code review completo
     */
    public String performCodeReview(String code, String fileName, String model, String criteria) {
        log.info("Realizando code review com modelo: {} para arquivo: {}", model, fileName);

        if (!llmEnabled) {
            return generateMockCodeReview(code, fileName);
        }

        try {
            String prompt = buildCodeReviewPrompt(code, fileName, criteria);
            return callLlmApi(model, prompt);
        } catch (Exception e) {
            log.error("Erro ao realizar code review com LLM", e);
            return generateMockCodeReview(code, fileName);
        }
    }

    /**
     * Obt�m informa��es de um modelo espec�fico
     */
    public LlmModelDto getModelInfo(String modelId) {
        return modelCache.get(modelId);
    }

    /**
     * Verifica sa�de dos servi�os LLM
     */
    public boolean checkHealth() {
        if (!llmEnabled) {
            return true; // Em modo mock sempre est� "saud�vel"
        }

        boolean geminiHealthy = !isGeminiConfigured() || checkGeminiHealth();
        boolean openaiHealthy = !isOpenAiConfigured() || checkOpenAiHealth();

        return geminiHealthy && openaiHealthy;
    }

    // =================== M�TODOS PRIVADOS ===================

    private boolean isGeminiConfigured() {
        return geminiApiKey != null && !geminiApiKey.trim().isEmpty();
    }

    private boolean isOpenAiConfigured() {
        return openaiApiKey != null && !openaiApiKey.trim().isEmpty();
    }

    private boolean checkGeminiHealth() {
        try {
            // Implementar verifica��o de sa�de do Gemini
            return true;
        } catch (Exception e) {
            log.warn("Gemini n�o est� dispon�vel: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkOpenAiHealth() {
        try {
            // Implementar verifica��o de sa�de do OpenAI
            return true;
        } catch (Exception e) {
            log.warn("OpenAI n�o est� dispon�vel: {}", e.getMessage());
            return false;
        }
    }

    private String callLlmApi(String model, String prompt) {
        if (model.startsWith("gemini")) {
            return callGeminiApi(model, prompt);
        } else if (model.startsWith("gpt")) {
            return callOpenAiApi(model, prompt);
        } else {
            throw new IllegalArgumentException("Modelo n�o suportado: " + model);
        }
    }

    private String callGeminiApi(String model, String prompt) {
        // Implementa��o simplificada - em produ��o usar biblioteca oficial
        log.info("Simulando chamada para Gemini API com modelo: {}", model);
        return "An�lise simulada do Gemini para o c�digo fornecido. Em produ��o, esta seria uma chamada real para a API do Google.";
    }

    private String callOpenAiApi(String model, String prompt) {
        // Implementa��o simplificada - em produ��o usar biblioteca oficial
        log.info("Simulando chamada para OpenAI API com modelo: {}", model);
        return "An�lise simulada do OpenAI GPT para o c�digo fornecido. Em produ��o, esta seria uma chamada real para a API da OpenAI.";
    }

    private String buildAnalysisPrompt(String code, String fileName) {
        return String.format("""
            Analise o seguinte c�digo Java e forne�a uma an�lise detalhada:
            
            Arquivo: %s
            
            C�digo:
            ```java
            %s
            ```
            
            Por favor, analise:
            1. Qualidade do c�digo
            2. Poss�veis bugs ou problemas
            3. Quest�es de seguran�a
            4. Performance
            5. Padr�es de codifica��o da Sinqia
            6. Verificar padr�o empresta()/devolve() para conex�es
            
            Forne�a uma an�lise estruturada em portugu�s brasileiro.
            """, fileName, code);
    }

    private String buildSuggestionsPrompt(String code, String fileName) {
        return String.format("""
            Gere sugest�es de melhoria para o seguinte c�digo Java:
            
            Arquivo: %s
            
            C�digo:
            ```java
            %s
            ```
            
            Forne�a sugest�es espec�ficas para:
            1. Melhorias de performance
            2. Melhor legibilidade
            3. Conformidade com padr�es Sinqia
            4. Tratamento de erros
            5. Seguran�a
            
            Liste as sugest�es em ordem de prioridade em portugu�s brasileiro.
            """, fileName, code);
    }

    private String buildCodeReviewPrompt(String code, String fileName, String criteria) {
        String baseCriteria = criteria != null ? criteria : "padr�es gerais de qualidade";
        
        return String.format("""
            Realize um code review completo do seguinte c�digo Java:
            
            Arquivo: %s
            Crit�rios: %s
            
            C�digo:
            ```java
            %s
            ```
            
            Forne�a um review detalhado incluindo:
            1. Pontos fortes do c�digo
            2. Problemas identificados
            3. Sugest�es de melhoria
            4. Nota geral (0-10)
            5. Conformidade com padr�es Sinqia
            
            Resposta em portugu�s brasileiro com formato estruturado.
            """, fileName, baseCriteria, code);
    }

    // =================== M�TODOS MOCK PARA DESENVOLVIMENTO ===================

    private String generateMockAnalysis(String code, String fileName, String model) {
        return String.format("""
            ## An�lise de C�digo - %s
            
            **Modelo utilizado:** %s
            **Arquivo:** %s
            
            ### Resumo da An�lise
            O c�digo analisado apresenta qualidade geral boa, mas foram identificadas algumas oportunidades de melhoria.
            
            ### Principais Achados
            
            #### ? Pontos Positivos
            - Estrutura de classes bem definida
            - Nomenclatura de vari�veis adequada
            - Uso correto de modificadores de acesso
            
            #### ?? �reas de Aten��o
            - Verificar implementa��o do padr�o empresta()/devolve() para conex�es
            - Considerar tratamento de exce��es mais espec�fico
            - Avaliar necessidade de valida��o de entrada
            
            #### ?? Recomenda��es Sinqia
            - Seguir padr�es corporativos de logging
            - Implementar valida��es de neg�cio adequadas
            - Documentar m�todos p�blicos com JavaDoc
            
            ### Pontua��o Geral: 7.5/10
            
            *Esta � uma an�lise simulada. Configure as chaves de API para an�lises reais com IA.*
            """, fileName, model, fileName);
    }

    private String generateMockSuggestions(String code, String fileName) {
        return """
            ## Sugest�es de Melhoria
            
            ### ?? Alta Prioridade
            1. **Implementar padr�o empresta()/devolve()** - Essencial para evitar vazamentos de conex�o
            2. **Adicionar tratamento de exce��es** - Melhorar robustez da aplica��o
            3. **Validar par�metros de entrada** - Prevenir erros em tempo de execu��o
            
            ### ?? M�dia Prioridade
            4. **Adicionar logging estruturado** - Facilitar debugging e monitoramento
            5. **Implementar testes unit�rios** - Garantir qualidade e cobertura
            6. **Documentar API p�blica** - Melhorar manutenibilidade
            
            ### ?? Baixa Prioridade
            7. **Refatorar m�todos longos** - Melhorar legibilidade
            8. **Otimizar imports** - Remover depend�ncias desnecess�rias
            9. **Adicionar constantes** - Evitar magic numbers e strings
            
            *Sugest�es geradas em modo simulado. Configure as chaves de API para sugest�es personalizadas com IA.*
            """;
    }

    private String generateMockCodeReview(String code, String fileName) {
        return String.format("""
            ## Code Review - %s
            
            ### ?? Avalia��o Geral
            **Nota:** 8.0/10
            **Status:** Aprovado com ressalvas
            
            ### ? Pontos Fortes
            - C�digo bem estruturado e organizado
            - Nomenclatura clara e consistente
            - Separa��o adequada de responsabilidades
            - Boa utiliza��o de modificadores de acesso
            
            ### ?? Observa��es
            
            #### Funcionalidade
            - ? L�gica implementada corretamente
            - ?? Verificar padr�es espec�ficos da Sinqia
            - ?? Considerar edge cases adicionais
            
            #### Qualidade do C�digo
            - ? Estrutura clara e bem organizada
            - ?? Alguns m�todos podem ser simplificados
            - ?? Documenta��o pode ser melhorada
            
            #### Seguran�a
            - ? Sem vulnerabilidades �bvias
            - ?? Validar entrada de dados do usu�rio
            - ?? Implementar logging de seguran�a
            
            ### ?? A��es Recomendadas
            1. Implementar valida��o robusta de entrada
            2. Adicionar tratamento espec�fico de exce��es
            3. Incluir testes unit�rios abrangentes
            4. Documentar m�todos p�blicos
            
            ### ?? Conformidade Sinqia
            - ? Estrutura de pacotes adequada
            - ?? Verificar padr�o empresta()/devolve()
            - ? Nomenclatura segue conven��es
            
            **Recomenda��o:** C�digo aprovado ap�s implementar as corre��es sugeridas.
            
            *Review gerado em modo simulado. Configure as chaves de API para reviews personalizados com IA.*
            """, fileName);
    }
}