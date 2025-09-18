package com.sinqia.maya.dto;

import com.sinqia.maya.entity.AnalysisIssue;
import java.time.LocalDateTime;

public class AnalysisIssueDto {
    
    private Long id;
    private AnalysisIssue.IssueSeverity severity;
    private AnalysisIssue.IssueType type;
    private String title;
    private String description;
    private Integer lineNumber;
    private String codeSnippet;
    private String recommendation;
    private LocalDateTime createdAt;
    
    // Constructors
    public AnalysisIssueDto() {}
    
    public AnalysisIssueDto(AnalysisIssue entity) {
        this.id = entity.getId();
        this.severity = entity.getSeverity();
        this.type = entity.getType();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.lineNumber = entity.getLineNumber();
        this.codeSnippet = entity.getCodeSnippet();
        this.recommendation = entity.getRecommendation();
        this.createdAt = entity.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AnalysisIssue.IssueSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(AnalysisIssue.IssueSeverity severity) {
        this.severity = severity;
    }
    
    public AnalysisIssue.IssueType getType() {
        return type;
    }
    
    public void setType(AnalysisIssue.IssueType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getCodeSnippet() {
        return codeSnippet;
    }
    
    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
