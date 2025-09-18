package com.sinqia.maya.dto;

import com.sinqia.maya.entity.AnalysisSettings.AnalysisType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferência de dados de configurações de análise
 */
public class AnalysisSettingsDto {
    
    private Long id;
    
    @NotNull(message = "Tipo de análise é obrigatório")
    private AnalysisType analysisType;
    
    @NotBlank(message = "Template do prompt é obrigatório")
    @Size(min = 10, max = 10000, message = "Template deve ter entre 10 e 10000 caracteres")
    private String promptTemplate;
    
    private String markdownContent;
    
    private String filename;
    
    @NotNull(message = "Status ativo é obrigatório")
    private Boolean isActive;
    
    @NotEmpty(message = "Pelo menos uma extensão de arquivo deve ser especificada")
    private List<String> fileExtensions;
    
    @Min(value = 100, message = "Máximo de tokens deve ser pelo menos 100")
    @Max(value = 4000, message = "Máximo de tokens não pode exceder 4000")
    private Integer maxTokens;
    
    @DecimalMin(value = "0.0", message = "Temperatura deve ser pelo menos 0.0")
    @DecimalMax(value = "1.0", message = "Temperatura não pode exceder 1.0")
    private Double temperature;
    
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public AnalysisSettingsDto() {}
    
    public AnalysisSettingsDto(AnalysisType analysisType, String promptTemplate, Boolean isActive) {
        this.analysisType = analysisType;
        this.promptTemplate = promptTemplate;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AnalysisType getAnalysisType() {
        return analysisType;
    }
    
    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }
    
    public String getPromptTemplate() {
        return promptTemplate;
    }
    
    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }
    
    public String getMarkdownContent() {
        return markdownContent;
    }
    
    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public List<String> getFileExtensions() {
        return fileExtensions;
    }
    
    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
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
    
    @Override
    public String toString() {
        return "AnalysisSettingsDto{" +
                "id=" + id +
                ", analysisType=" + analysisType +
                ", filename='" + filename + '\'' +
                ", isActive=" + isActive +
                ", fileExtensions=" + fileExtensions +
                ", maxTokens=" + maxTokens +
                ", temperature=" + temperature +
                '}';
    }
}