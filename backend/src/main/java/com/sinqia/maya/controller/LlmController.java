package com.sinqia.maya.controller;

import com.sinqia.maya.dto.CodeAnalysisRequestDto;
import com.sinqia.maya.dto.LlmModelDto;
import com.sinqia.maya.dto.LlmResponseDto;
import com.sinqia.maya.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller para operações de análise de código com LLM (Large Language Models)
 * Implementa integração com modelos de IA como Gemini, GPT, etc.
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4500"})
public class LlmController {

    private final LlmService llmService;

    /**
     * Lista todos os modelos LLM disponíveis
     */
    @GetMapping("/models")
    public ResponseEntity<LlmResponseDto> getAvailableModels() {
        log.info("Requisição para listar modelos LLM disponíveis");
        
        try {
            List<String> models = llmService.getAvailableModels();
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("Modelos LLM carregados com sucesso")
                    .models(models)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao carregar modelos LLM", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro ao carregar modelos: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Analisa código usando modelo LLM especificado
     */
    @PostMapping("/analyze")
    public ResponseEntity<LlmResponseDto> analyzeCode(@Valid @RequestBody CodeAnalysisRequestDto request) {
        log.info("Requisição de análise de código com modelo: {}", request.getModel());
        
        try {
            String analysis = llmService.analyzeCode(
                request.getCode(), 
                request.getFileName(), 
                request.getModel()
            );
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("Análise de código concluída")
                    .analysis(analysis)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao analisar código com LLM", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro na análise: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Gera sugestões de melhoria para o código
     */
    @PostMapping("/suggestions")
    public ResponseEntity<LlmResponseDto> generateSuggestions(@Valid @RequestBody CodeAnalysisRequestDto request) {
        log.info("Requisição de sugestões para código com modelo: {}", request.getModel());
        
        try {
            String suggestions = llmService.generateSuggestions(
                request.getCode(), 
                request.getFileName(), 
                request.getModel()
            );
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("Sugestões geradas com sucesso")
                    .suggestions(suggestions)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao gerar sugestões com LLM", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro ao gerar sugestões: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Realiza code review completo usando LLM
     */
    @PostMapping("/review")
    public ResponseEntity<LlmResponseDto> performCodeReview(@Valid @RequestBody CodeAnalysisRequestDto request) {
        log.info("Requisição de code review com modelo: {}", request.getModel());
        
        try {
            String review = llmService.performCodeReview(
                request.getCode(), 
                request.getFileName(), 
                request.getModel(),
                request.getCriteria()
            );
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("Code review concluído")
                    .review(review)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao realizar code review com LLM", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro no code review: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtém informações detalhadas de um modelo específico
     */
    @GetMapping("/models/{modelId}")
    public ResponseEntity<LlmModelDto> getModelInfo(@PathVariable String modelId) {
        log.info("Requisição de informações do modelo: {}", modelId);
        
        try {
            LlmModelDto modelInfo = llmService.getModelInfo(modelId);
            return ResponseEntity.ok(modelInfo);
            
        } catch (Exception e) {
            log.error("Erro ao obter informações do modelo", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Testa conectividade com APIs de LLM
     */
    @GetMapping("/health")
    public ResponseEntity<LlmResponseDto> checkHealth() {
        log.info("Verificação de saúde dos serviços LLM");
        
        try {
            boolean isHealthy = llmService.checkHealth();
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status(isHealthy ? "SUCCESS" : "WARNING")
                    .message(isHealthy ? "Todos os serviços LLM operacionais" : "Alguns serviços indisponíveis")
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro na verificação de saúde dos LLMs", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro na verificação: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }
}