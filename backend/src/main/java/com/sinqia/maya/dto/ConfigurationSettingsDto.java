package com.sinqia.maya.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para configurações do sistema MAYA
 */
public class ConfigurationSettingsDto {

    private Long id;

    // Configurações TFS
    @Size(max = 500, message = "URL do servidor TFS deve ter no máximo 500 caracteres")
    private String tfsServerUrl;

    @Size(max = 200, message = "Projeto TFS deve ter no máximo 200 caracteres")
    private String tfsProject;

    @Size(max = 100, message = "Nome de usuário TFS deve ter no máximo 100 caracteres")
    private String tfsUsername;

    @Size(max = 500, message = "Senha TFS deve ter no máximo 500 caracteres")
    private String tfsPassword;

    @Size(max = 500, message = "Token de acesso pessoal deve ter no máximo 500 caracteres")
    private String tfsPersonalAccessToken;

    @Size(max = 200, message = "Coleção TFS deve ter no máximo 200 caracteres")
    private String tfsCollection;

    // Configurações de Prompt
    @NotBlank(message = "Prompt personalizado é obrigatório")
    private String customPrompt;

    @Size(max = 200, message = "Nome do arquivo de prompt deve ter no máximo 200 caracteres")
    private String promptFilename;

    // Configurações gerais
    @NotNull(message = "Status ativo é obrigatório")
    private Boolean isActive;

    @Size(max = 100, message = "Criado por deve ter no máximo 100 caracteres")
    private String createdBy;

    @Size(max = 100, message = "Atualizado por deve ter no máximo 100 caracteres")
    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Size(max = 10, message = "Máximo de 10 arquivos auxiliares permitidos")
    private List<AuxiliaryFileDto> auxiliaryFiles;

    // Construtores
    public ConfigurationSettingsDto() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTfsServerUrl() {
        return tfsServerUrl;
    }

    public void setTfsServerUrl(String tfsServerUrl) {
        this.tfsServerUrl = tfsServerUrl;
    }

    public String getTfsProject() {
        return tfsProject;
    }

    public void setTfsProject(String tfsProject) {
        this.tfsProject = tfsProject;
    }

    public String getTfsUsername() {
        return tfsUsername;
    }

    public void setTfsUsername(String tfsUsername) {
        this.tfsUsername = tfsUsername;
    }

    public String getTfsPassword() {
        return tfsPassword;
    }

    public void setTfsPassword(String tfsPassword) {
        this.tfsPassword = tfsPassword;
    }

    public String getTfsPersonalAccessToken() {
        return tfsPersonalAccessToken;
    }

    public void setTfsPersonalAccessToken(String tfsPersonalAccessToken) {
        this.tfsPersonalAccessToken = tfsPersonalAccessToken;
    }

    public String getTfsCollection() {
        return tfsCollection;
    }

    public void setTfsCollection(String tfsCollection) {
        this.tfsCollection = tfsCollection;
    }

    public String getCustomPrompt() {
        return customPrompt;
    }

    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }

    public String getPromptFilename() {
        return promptFilename;
    }

    public void setPromptFilename(String promptFilename) {
        this.promptFilename = promptFilename;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AuxiliaryFileDto> getAuxiliaryFiles() {
        return auxiliaryFiles;
    }

    public void setAuxiliaryFiles(List<AuxiliaryFileDto> auxiliaryFiles) {
        this.auxiliaryFiles = auxiliaryFiles;
    }
}
