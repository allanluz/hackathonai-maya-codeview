package com.sinqia.maya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO para requisi��es de an�lise de c�digo com LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeAnalysisRequestDto {

    @NotBlank(message = "C�digo � obrigat�rio")
    @Size(min = 10, max = 50000, message = "C�digo deve ter entre 10 e 50.000 caracteres")
    private String code;

    @NotBlank(message = "Nome do arquivo � obrigat�rio")
    @Size(max = 255, message = "Nome do arquivo deve ter no m�ximo 255 caracteres")
    private String fileName;

    @NotBlank(message = "Modelo LLM � obrigat�rio")
    private String model;

    /**
     * Crit�rios espec�ficos para an�lise (opcional)
     */
    private String criteria;

    /**
     * Tipo de an�lise solicitada
     */
    private String analysisType;

    /**
     * Configura��es adicionais para a an�lise
     */
    private String options;
}