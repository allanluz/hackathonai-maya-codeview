package com.sinqia.maya.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para arquivos auxiliares do sistema MAYA
 */
public class AuxiliaryFileDto {

    private Long id;

    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 200, message = "Nome do arquivo deve ter no máximo 200 caracteres")
    private String filename;

    @NotBlank(message = "Nome original do arquivo é obrigatório")
    @Size(max = 200, message = "Nome original do arquivo deve ter no máximo 200 caracteres")
    private String originalFilename;

    @NotBlank(message = "Conteúdo do arquivo é obrigatório")
    private String fileContent;

    @Max(value = 5242880, message = "Tamanho máximo do arquivo é 5MB")
    private Long fileSize;

    @Pattern(regexp = "^text/markdown$|^text/plain$", message = "Tipo de arquivo deve ser markdown (.md) ou texto plano")
    private String fileType;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @Min(value = 1, message = "Ordem do arquivo deve ser maior que 0")
    @Max(value = 10, message = "Ordem do arquivo deve ser menor ou igual a 10")
    private Integer fileOrder;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean isActive;

    @Size(max = 100, message = "Enviado por deve ter no máximo 100 caracteres")
    private String uploadedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long configurationId;

    // Construtores
    public AuxiliaryFileDto() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(Integer fileOrder) {
        this.fileOrder = fileOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
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

    public Long getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Long configurationId) {
        this.configurationId = configurationId;
    }
}
