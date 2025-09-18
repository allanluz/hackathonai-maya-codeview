package com.sinqia.maya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO com informações detalhadas de um modelo LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModelDto {

    /**
     * Identificador único do modelo
     */
    private String id;

    /**
     * Nome amigável do modelo
     */
    private String name;

    /**
     * Família/provedor do modelo (Google, OpenAI, etc.)
     */
    private String family;

    /**
     * Descrição das capacidades do modelo
     */
    private String description;

    /**
     * Versão do modelo
     */
    private String version;

    /**
     * Limite máximo de tokens suportado
     */
    private Integer maxTokens;

    /**
     * Indica se o modelo está disponível
     */
    private Boolean available;

    /**
     * Tipo de modelo (text, code, multimodal)
     */
    private String type;

    /**
     * Custo estimado por uso
     */
    private String cost;

    /**
     * Tempo médio de resposta em ms
     */
    private Integer avgResponseTime;

    /**
     * Data da última atualização do modelo
     */
    private String lastUpdated;
}