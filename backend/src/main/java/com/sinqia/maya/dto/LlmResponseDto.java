package com.sinqia.maya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respostas de operações LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponseDto {

    /**
     * Status da operação: SUCCESS, ERROR, WARNING
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
     * Lista de modelos disponíveis (usado em /models)
     */
    private List<String> models;

    /**
     * Resultado da análise de código (usado em /analyze)
     */
    private String analysis;

    /**
     * Sugestões de melhoria (usado em /suggestions)
     */
    private String suggestions;

    /**
     * Resultado do code review (usado em /review)
     */
    private String review;

    /**
     * Informações adicionais ou metadados
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
     * Cria resposta de sucesso com análise
     */
    public static LlmResponseDto successWithAnalysis(String analysis) {
        return LlmResponseDto.builder()
                .status("SUCCESS")
                .message("Análise concluída com sucesso")
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