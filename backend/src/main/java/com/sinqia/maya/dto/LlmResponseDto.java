package com.sinqia.maya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respostas de opera��es LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponseDto {

    /**
     * Status da opera��o: SUCCESS, ERROR, WARNING
     */
    private String status;

    /**
     * Mensagem descritiva do resultado
     */
    private String message;

    /**
     * Timestamp da resposta
     */
    private Long timestamp;

    /**
     * Lista de modelos dispon�veis (usado em /models)
     */
    private List<String> models;

    /**
     * Resultado da an�lise de c�digo (usado em /analyze)
     */
    private String analysis;

    /**
     * Sugest�es de melhoria (usado em /suggestions)
     */
    private String suggestions;

    /**
     * Resultado do code review (usado em /review)
     */
    private String review;

    /**
     * Informa��es adicionais ou metadados
     */
    private Object metadata;

    /**
     * Cria resposta de sucesso simples
     */
    public static LlmResponseDto success(String message) {
        return LlmResponseDto.builder()
                .status("SUCCESS")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Cria resposta de erro
     */
    public static LlmResponseDto error(String message) {
        return LlmResponseDto.builder()
                .status("ERROR")
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Cria resposta de sucesso com an�lise
     */
    public static LlmResponseDto successWithAnalysis(String analysis) {
        return LlmResponseDto.builder()
                .status("SUCCESS")
                .message("An�lise conclu�da com sucesso")
                .analysis(analysis)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Cria resposta de sucesso com modelos
     */
    public static LlmResponseDto successWithModels(List<String> models) {
        return LlmResponseDto.builder()
                .status("SUCCESS")
                .message("Modelos carregados com sucesso")
                .models(models)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}