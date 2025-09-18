package com.sinqia.maya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO para requisições de análise de código com LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeAnalysisRequestDto {

    @NotBlank(message = "Código é obrigatório")
    @Size(min = 10, max = 50000, message = "Código deve ter entre 10 e 50.000 caracteres")
    private String code;

    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 255, message = "Nome do arquivo deve ter no máximo 255 caracteres")
    private String fileName;

    @NotBlank(message = "Modelo LLM é obrigatório")
    private String model;

    /**
     * Critérios específicos para análise (opcional)
     */
    private String criteria;

    /**
     * Tipo de análise solicitada
     */
    private String analysisType;

    /**
     * Configurações adicionais para a análise
     */
    private String options;
}