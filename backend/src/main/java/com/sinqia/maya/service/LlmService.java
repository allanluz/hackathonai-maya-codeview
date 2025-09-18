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
 * Serviço para integração com modelos LLM (Large Language Models)
 * Suporta integração com Gemini (Google), GPT (OpenAI), etc.
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
     * Inicializa cache de modelos disponíveis
     */
    private void initializeModels() {
        // Modelos Gemini disponíveis
        if (isGeminiConfigured()) {
            modelCache.put("gemini-2.0-flash-001", LlmModelDto.builder()
                    .id("gemini-2.0-flash-001")
                    .name("Gemini 2.0 Flash")
                    .family("Google")
                    .description("Modelo avançado da Google para análise de código e geração de texto")
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

        // Modelos OpenAI disponíveis
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
                    .description("Modelo GPT-3.5 rápido e eficiente")
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
     * Obtém lista de modelos LLM disponíveis
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

        // Se não há modelos configurados, retorna fallback
        if (availableModels.isEmpty()) {
            return getFallbackModels();
        }

        return availableModels;
    }

    /**
     * Modelos fallback para desenvolvimento/demonstração
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
     * Analisa código usando modelo LLM especificado
     */
    public String analyzeCode(String code, String fileName, String model) {
        log.info("Analisando código com modelo: {} para arquivo: {}", model, fileName);

        if (!llmEnabled) {
            return generateMockAnalysis(code, fileName, model);
        }

        try {
            String prompt = buildAnalysisPrompt(code, fileName);
            return callLlmApi(model, prompt);
        } catch (Exception e) {
            log.error("Erro ao analisar código com LLM", e);
            return generateMockAnalysis(code, fileName, model);
        }
    }

    /**
     * Gera sugestões de melhoria para o código
     */
    public String generateSuggestions(String code, String fileName, String model) {
        log.info("Gerando sugestões com modelo: {} para arquivo: {}", model, fileName);

        if (!llmEnabled) {
            return generateMockSuggestions(code, fileName);
        }

        try {
            String prompt = buildSuggestionsPrompt(code, fileName);
            return callLlmApi(model, prompt);
        } catch (Exception e) {
            log.error("Erro ao gerar sugestões com LLM", e);
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
     * Obtém informações de um modelo específico
     */
    public LlmModelDto getModelInfo(String modelId) {
        return modelCache.get(modelId);
    }

    /**
     * Verifica saúde dos serviços LLM
     */
    public boolean checkHealth() {
        if (!llmEnabled) {
            return true; // Em modo mock sempre está "saudável"
        }

        boolean geminiHealthy = !isGeminiConfigured() || checkGeminiHealth();
        boolean openaiHealthy = !isOpenAiConfigured() || checkOpenAiHealth();

        return geminiHealthy && openaiHealthy;
    }

    // =================== MÉTODOS PRIVADOS ===================

    private boolean isGeminiConfigured() {
        return geminiApiKey != null && !geminiApiKey.trim().isEmpty();
    }

    private boolean isOpenAiConfigured() {
        return openaiApiKey != null && !openaiApiKey.trim().isEmpty();
    }

    private boolean checkGeminiHealth() {
        try {
            // Implementar verificação de saúde do Gemini
            return true;
        } catch (Exception e) {
            log.warn("Gemini não está disponível: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkOpenAiHealth() {
        try {
            // Implementar verificação de saúde do OpenAI
            return true;
        } catch (Exception e) {
            log.warn("OpenAI não está disponível: {}", e.getMessage());
            return false;
        }
    }

    private String callLlmApi(String model, String prompt) {
        if (model.startsWith("gemini")) {
            return callGeminiApi(model, prompt);
        } else if (model.startsWith("gpt")) {
            return callOpenAiApi(model, prompt);
        } else {
            throw new IllegalArgumentException("Modelo não suportado: " + model);
        }
    }

    private String callGeminiApi(String model, String prompt) {
        // Implementação simplificada - em produção usar biblioteca oficial
        log.info("Simulando chamada para Gemini API com modelo: {}", model);
        return "Análise simulada do Gemini para o código fornecido. Em produção, esta seria uma chamada real para a API do Google.";
    }

    private String callOpenAiApi(String model, String prompt) {
        // Implementação simplificada - em produção usar biblioteca oficial
        log.info("Simulando chamada para OpenAI API com modelo: {}", model);
        return "Análise simulada do OpenAI GPT para o código fornecido. Em produção, esta seria uma chamada real para a API da OpenAI.";
    }

    private String buildAnalysisPrompt(String code, String fileName) {
        return String.format("""
            Analise o seguinte código Java e forneça uma análise detalhada:
            
            Arquivo: %s
            
            Código:
            ```java
            %s
            ```
            
            Por favor, analise:
            1. Qualidade do código
            2. Possíveis bugs ou problemas
            3. Questões de segurança
            4. Performance
            5. Padrões de codificação da Sinqia
            6. Verificar padrão empresta()/devolve() para conexões
            
            Forneça uma análise estruturada em português brasileiro.
            """, fileName, code);
    }

    private String buildSuggestionsPrompt(String code, String fileName) {
        return String.format("""
            Gere sugestões de melhoria para o seguinte código Java:
            
            Arquivo: %s
            
            Código:
            ```java
            %s
            ```
            
            Forneça sugestões específicas para:
            1. Melhorias de performance
            2. Melhor legibilidade
            3. Conformidade com padrões Sinqia
            4. Tratamento de erros
            5. Segurança
            
            Liste as sugestões em ordem de prioridade em português brasileiro.
            """, fileName, code);
    }

    private String buildCodeReviewPrompt(String code, String fileName, String criteria) {
        String baseCriteria = criteria != null ? criteria : "padrões gerais de qualidade";
        
        return String.format("""
            Realize um code review completo do seguinte código Java:
            
            Arquivo: %s
            Critérios: %s
            
            Código:
            ```java
            %s
            ```
            
            Forneça um review detalhado incluindo:
            1. Pontos fortes do código
            2. Problemas identificados
            3. Sugestões de melhoria
            4. Nota geral (0-10)
            5. Conformidade com padrões Sinqia
            
            Resposta em português brasileiro com formato estruturado.
            """, fileName, baseCriteria, code);
    }

    // =================== MÉTODOS MOCK PARA DESENVOLVIMENTO ===================

    private String generateMockAnalysis(String code, String fileName, String model) {
        return String.format("""
            ## Análise de Código - %s
            
            **Modelo utilizado:** %s
            **Arquivo:** %s
            
            ### Resumo da Análise
            O código analisado apresenta qualidade geral boa, mas foram identificadas algumas oportunidades de melhoria.
            
            ### Principais Achados
            
            #### ? Pontos Positivos
            - Estrutura de classes bem definida
            - Nomenclatura de variáveis adequada
            - Uso correto de modificadores de acesso
            
            #### ?? Áreas de Atenção
            - Verificar implementação do padrão empresta()/devolve() para conexões
            - Considerar tratamento de exceções mais específico
            - Avaliar necessidade de validação de entrada
            
            #### ?? Recomendações Sinqia
            - Seguir padrões corporativos de logging
            - Implementar validações de negócio adequadas
            - Documentar métodos públicos com JavaDoc
            
            ### Pontuação Geral: 7.5/10
            
            *Esta é uma análise simulada. Configure as chaves de API para análises reais com IA.*
            """, fileName, model, fileName);
    }

    private String generateMockSuggestions(String code, String fileName) {
        return """
            ## Sugestões de Melhoria
            
            ### ?? Alta Prioridade
            1. **Implementar padrão empresta()/devolve()** - Essencial para evitar vazamentos de conexão
            2. **Adicionar tratamento de exceções** - Melhorar robustez da aplicação
            3. **Validar parâmetros de entrada** - Prevenir erros em tempo de execução
            
            ### ?? Média Prioridade
            4. **Adicionar logging estruturado** - Facilitar debugging e monitoramento
            5. **Implementar testes unitários** - Garantir qualidade e cobertura
            6. **Documentar API pública** - Melhorar manutenibilidade
            
            ### ?? Baixa Prioridade
            7. **Refatorar métodos longos** - Melhorar legibilidade
            8. **Otimizar imports** - Remover dependências desnecessárias
            9. **Adicionar constantes** - Evitar magic numbers e strings
            
            *Sugestões geradas em modo simulado. Configure as chaves de API para sugestões personalizadas com IA.*
            """;
    }

    private String generateMockCodeReview(String code, String fileName) {
        return String.format("""
            ## Code Review - %s
            
            ### ?? Avaliação Geral
            **Nota:** 8.0/10
            **Status:** Aprovado com ressalvas
            
            ### ? Pontos Fortes
            - Código bem estruturado e organizado
            - Nomenclatura clara e consistente
            - Separação adequada de responsabilidades
            - Boa utilização de modificadores de acesso
            
            ### ?? Observações
            
            #### Funcionalidade
            - ? Lógica implementada corretamente
            - ?? Verificar padrões específicos da Sinqia
            - ?? Considerar edge cases adicionais
            
            #### Qualidade do Código
            - ? Estrutura clara e bem organizada
            - ?? Alguns métodos podem ser simplificados
            - ?? Documentação pode ser melhorada
            
            #### Segurança
            - ? Sem vulnerabilidades óbvias
            - ?? Validar entrada de dados do usuário
            - ?? Implementar logging de segurança
            
            ### ?? Ações Recomendadas
            1. Implementar validação robusta de entrada
            2. Adicionar tratamento específico de exceções
            3. Incluir testes unitários abrangentes
            4. Documentar métodos públicos
            
            ### ?? Conformidade Sinqia
            - ? Estrutura de pacotes adequada
            - ?? Verificar padrão empresta()/devolve()
            - ? Nomenclatura segue convenções
            
            **Recomendação:** Código aprovado após implementar as correções sugeridas.
            
            *Review gerado em modo simulado. Configure as chaves de API para reviews personalizados com IA.*
            """, fileName);
    }
}