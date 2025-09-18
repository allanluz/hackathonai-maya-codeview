package com.sinqia.maya.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TfsCommitDto {
    private String commitId;
    private String commitSha;
    private String author;
    private String authorEmail;
    private String message;
    private LocalDateTime timestamp;
    private String branch;
    private String repository;
    private String project;
    private List<TfsFileChangeDto> files;
    
    // Construtores
    public TfsCommitDto() {}
    
    public TfsCommitDto(String commitId, String commitSha, String author, String message, 
                       LocalDateTime timestamp, String branch, List<TfsFileChangeDto> files) {
        this.commitId = commitId;
        this.commitSha = commitSha;
        this.author = author;
        this.message = message;
        this.timestamp = timestamp;
        this.branch = branch;
        this.files = files;
    }
    
    // Getters e Setters
    public String getCommitId() {
        return commitId;
    }
    
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    
    public String getCommitSha() {
        return commitSha;
    }
    
    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getBranch() {
        return branch;
    }
    
    public void setBranch(String branch) {
        this.branch = branch;
    }
    
    public String getRepository() {
        return repository;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    public String getProject() {
        return project;
    }
    
    public void setProject(String project) {
        this.project = project;
    }
    
    public List<TfsFileChangeDto> getFiles() {
        return files;
    }
    
    public void setFiles(List<TfsFileChangeDto> files) {
        this.files = files;
    }
}
