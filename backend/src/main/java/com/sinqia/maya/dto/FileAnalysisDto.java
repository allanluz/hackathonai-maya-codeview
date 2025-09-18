package com.sinqia.maya.dto;

import com.sinqia.maya.entity.FileAnalysis;
import java.time.LocalDateTime;
import java.util.List;

public class FileAnalysisDto {
    
    private Long id;
    private Long codeReviewId;
    private String filePath;
    private String className;
    private Integer linesAdded;
    private Integer linesRemoved;
    private Integer totalChanges;
    private Integer connectionEmpresta;
    private Integer connectionDevolve;
    private Boolean connectionBalanced;
    private Boolean hasTypeChanges;
    private Boolean hasMethodChanges;
    private Boolean hasValidationChanges;
    private String analysisResult;
    private String markdownReport;
    private LocalDateTime createdAt;
    private List<AnalysisIssueDto> issues;
    private boolean hasCriticalIssues;
    private long criticalIssuesCount;
    
    // Constructors
    public FileAnalysisDto() {}
    
    public FileAnalysisDto(FileAnalysis entity) {
        this.id = entity.getId();
        this.filePath = entity.getFilePath();
        this.className = entity.getClassName();
        this.linesAdded = entity.getLinesAdded();
        this.linesRemoved = entity.getLinesRemoved();
        this.totalChanges = entity.getTotalChanges();
        this.connectionEmpresta = entity.getConnectionEmpresta();
        this.connectionDevolve = entity.getConnectionDevolve();
        this.connectionBalanced = entity.getConnectionBalanced();
        this.hasTypeChanges = entity.getHasTypeChanges();
        this.hasMethodChanges = entity.getHasMethodChanges();
        this.hasValidationChanges = entity.getHasValidationChanges();
        this.analysisResult = entity.getAnalysisResult();
        this.markdownReport = entity.getMarkdownReport();
        this.createdAt = entity.getCreatedAt();
        this.hasCriticalIssues = entity.hasCriticalIssues();
        this.criticalIssuesCount = entity.getCriticalIssuesCount();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCodeReviewId() {
        return codeReviewId;
    }
    
    public void setCodeReviewId(Long codeReviewId) {
        this.codeReviewId = codeReviewId;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public Integer getLinesAdded() {
        return linesAdded;
    }
    
    public void setLinesAdded(Integer linesAdded) {
        this.linesAdded = linesAdded;
    }
    
    public Integer getLinesRemoved() {
        return linesRemoved;
    }
    
    public void setLinesRemoved(Integer linesRemoved) {
        this.linesRemoved = linesRemoved;
    }
    
    public Integer getTotalChanges() {
        return totalChanges;
    }
    
    public void setTotalChanges(Integer totalChanges) {
        this.totalChanges = totalChanges;
    }
    
    public Integer getConnectionEmpresta() {
        return connectionEmpresta;
    }
    
    public void setConnectionEmpresta(Integer connectionEmpresta) {
        this.connectionEmpresta = connectionEmpresta;
    }
    
    public void setConnectionEmpresa(Integer connectionEmpresa) {
        this.connectionEmpresta = connectionEmpresa;
    }
    
    public Integer getConnectionDevolve() {
        return connectionDevolve;
    }
    
    public void setConnectionDevolve(Integer connectionDevolve) {
        this.connectionDevolve = connectionDevolve;
    }
    
    public Boolean getConnectionBalanced() {
        return connectionBalanced;
    }
    
    public void setConnectionBalanced(Boolean connectionBalanced) {
        this.connectionBalanced = connectionBalanced;
    }
    
    public Boolean getHasTypeChanges() {
        return hasTypeChanges;
    }
    
    public void setHasTypeChanges(Boolean hasTypeChanges) {
        this.hasTypeChanges = hasTypeChanges;
    }
    
    public Boolean getHasMethodChanges() {
        return hasMethodChanges;
    }
    
    public void setHasMethodChanges(Boolean hasMethodChanges) {
        this.hasMethodChanges = hasMethodChanges;
    }
    
    public Boolean getHasValidationChanges() {
        return hasValidationChanges;
    }
    
    public void setHasValidationChanges(Boolean hasValidationChanges) {
        this.hasValidationChanges = hasValidationChanges;
    }
    
    public String getAnalysisResult() {
        return analysisResult;
    }
    
    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }
    
    public String getMarkdownReport() {
        return markdownReport;
    }
    
    public void setMarkdownReport(String markdownReport) {
        this.markdownReport = markdownReport;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<AnalysisIssueDto> getIssues() {
        return issues;
    }
    
    public void setIssues(List<AnalysisIssueDto> issues) {
        this.issues = issues;
    }
    
    public boolean isHasCriticalIssues() {
        return hasCriticalIssues;
    }
    
    public void setHasCriticalIssues(boolean hasCriticalIssues) {
        this.hasCriticalIssues = hasCriticalIssues;
    }
    
    public long getCriticalIssuesCount() {
        return criticalIssuesCount;
    }
    
    public void setCriticalIssuesCount(long criticalIssuesCount) {
        this.criticalIssuesCount = criticalIssuesCount;
    }
}
