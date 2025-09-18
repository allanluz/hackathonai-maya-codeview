package com.sinqia.maya.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sinqia.maya.entity.CodeReview;

public class CodeReviewDto {
    
    private Long id;
    private String commitSha;
    private String repositoryName;
    private String projectName;
    private String author;
    private String title;
    private String commitMessage;
    private String description;
    private CodeReview.ReviewStatus status;
    private Integer criticalIssues;
    private Integer totalIssues;
    private Integer filesAnalyzed;
    private Double analysisScore;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    private List<FileAnalysisDto> fileAnalyses;
    private boolean approved;
    
    // Constructors
    public CodeReviewDto() {}
    
    public CodeReviewDto(CodeReview entity) {
        this.id = entity.getId();
        this.commitSha = entity.getCommitSha();
        this.repositoryName = entity.getRepositoryName();
        this.projectName = entity.getProjectName();
        this.author = entity.getAuthor();
        this.title = entity.getTitle();
        this.commitMessage = entity.getCommitMessage();
        this.description = entity.getDescription();
        this.status = entity.getStatus();
        this.criticalIssues = entity.getCriticalIssues();
        this.totalIssues = entity.getTotalIssues();
        this.filesAnalyzed = entity.getFilesAnalyzed();
        this.analysisScore = entity.getAnalysisScore();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
        this.completedAt = entity.getCompletedAt();
        this.approved = entity.isApproved();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCommitSha() {
        return commitSha;
    }
    
    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }
    
    public String getRepositoryName() {
        return repositoryName;
    }
    
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
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
    
    public String getCommitMessage() {
        return commitMessage;
    }
    
    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
    
    public CodeReview.ReviewStatus getStatus() {
        return status;
    }
    
    public void setStatus(CodeReview.ReviewStatus status) {
        this.status = status;
    }
    
    public Integer getCriticalIssues() {
        return criticalIssues;
    }
    
    public void setCriticalIssues(Integer criticalIssues) {
        this.criticalIssues = criticalIssues;
    }
    
    public Integer getTotalIssues() {
        return totalIssues;
    }
    
    public void setTotalIssues(Integer totalIssues) {
        this.totalIssues = totalIssues;
    }
    
    public Integer getFilesAnalyzed() {
        return filesAnalyzed;
    }
    
    public void setFilesAnalyzed(Integer filesAnalyzed) {
        this.filesAnalyzed = filesAnalyzed;
    }
    
    public Double getAnalysisScore() {
        return analysisScore;
    }
    
    public void setAnalysisScore(Double analysisScore) {
        this.analysisScore = analysisScore;
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
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public List<FileAnalysisDto> getFileAnalyses() {
        return fileAnalyses;
    }
    
    public void setFileAnalyses(List<FileAnalysisDto> fileAnalyses) {
        this.fileAnalyses = fileAnalyses;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
