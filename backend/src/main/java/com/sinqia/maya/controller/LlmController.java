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
 * Controller para opera��es de an�lise de c�digo com LLM (Large Language Models)
 * Implementa integra��o com modelos de IA como Gemini, GPT, etc.
 */
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4500"})
public class LlmController {

    private final LlmService llmService;

    /**
     * Lista todos os modelos LLM dispon�veis
     */
    @GetMapping("/models")
    public ResponseEntity<LlmResponseDto> getAvailableModels() {
        log.info("Requisi��o para listar modelos LLM dispon�veis");
        
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
     * Analisa c�digo usando modelo LLM especificado
     */
    @PostMapping("/analyze")
    public ResponseEntity<LlmResponseDto> analyzeCode(@Valid @RequestBody CodeAnalysisRequestDto request) {
        log.info("Requisi��o de an�lise de c�digo com modelo: {}", request.getModel());
        
        try {
            String analysis = llmService.analyzeCode(
                request.getCode(), 
                request.getFileName(), 
                request.getModel()
            );
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("An�lise de c�digo conclu�da")
                    .analysis(analysis)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao analisar c�digo com LLM", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro na an�lise: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Gera sugest�es de melhoria para o c�digo
     */
    @PostMapping("/suggestions")
    public ResponseEntity<LlmResponseDto> generateSuggestions(@Valid @RequestBody CodeAnalysisRequestDto request) {
        log.info("Requisi��o de sugest�es para c�digo com modelo: {}", request.getModel());
        
        try {
            String suggestions = llmService.generateSuggestions(
                request.getCode(), 
                request.getFileName(), 
                request.getModel()
            );
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("Sugest�es geradas com sucesso")
                    .suggestions(suggestions)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao gerar sugest�es com LLM", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro ao gerar sugest�es: " + e.getMessage())
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
        log.info("Requisi��o de code review com modelo: {}", request.getModel());
        
        try {
            String review = llmService.performCodeReview(
                request.getCode(), 
                request.getFileName(), 
                request.getModel(),
                request.getCriteria()
            );
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("SUCCESS")
                    .message("Code review conclu�do")
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
     * Obt�m informa��es detalhadas de um modelo espec�fico
     */
    @GetMapping("/models/{modelId}")
    public ResponseEntity<LlmModelDto> getModelInfo(@PathVariable String modelId) {
        log.info("Requisi��o de informa��es do modelo: {}", modelId);
        
        try {
            LlmModelDto modelInfo = llmService.getModelInfo(modelId);
            return ResponseEntity.ok(modelInfo);
            
        } catch (Exception e) {
            log.error("Erro ao obter informa��es do modelo", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Testa conectividade com APIs de LLM
     */
    @GetMapping("/health")
    public ResponseEntity<LlmResponseDto> checkHealth() {
        log.info("Verifica��o de sa�de dos servi�os LLM");
        
        try {
            boolean isHealthy = llmService.checkHealth();
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status(isHealthy ? "SUCCESS" : "WARNING")
                    .message(isHealthy ? "Todos os servi�os LLM operacionais" : "Alguns servi�os indispon�veis")
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro na verifica��o de sa�de dos LLMs", e);
            
            LlmResponseDto response = LlmResponseDto.builder()
                    .status("ERROR")
                    .message("Erro na verifica��o: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(500).body(response);
        }
    }
}