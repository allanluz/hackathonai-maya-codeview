package com.sinqia.maya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO com informa��es detalhadas de um modelo LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmModelDto {

    /**
     * Identificador �nico do modelo
     */
    private String id;

    /**
     * Nome amig�vel do modelo
     */
    private String name;

    /**
     * Fam�lia/provedor do modelo (Google, OpenAI, etc.)
     */
    private String family;

    /**
     * Descri��o das capacidades do modelo
     */
    private String description;

    /**
     * Vers�o do modelo
     */
    private String version;

    /**
     * Limite m�ximo de tokens suportado
     */
    private Integer maxTokens;

    /**
     * Indica se o modelo est� dispon�vel
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
     * Tempo m�dio de resposta em ms
     */
    private Integer avgResponseTime;

    /**
     * Data da �ltima atualiza��o do modelo
     */
    private String lastUpdated;
}