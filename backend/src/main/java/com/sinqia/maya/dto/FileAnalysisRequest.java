package com.sinqia.maya.dto;

public class FileAnalysisRequest {
    
    private String path;
    private String content;
    private String language;
    
    // Constructors
    public FileAnalysisRequest() {}
    
    public FileAnalysisRequest(String path, String content, String language) {
        this.path = path;
        this.content = content;
        this.language = language;
    }
    
    // Getters and Setters
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    // Alias method for compatibility
    public String getFilePath() {
        return path;
    }
    
    public void setFilePath(String path) {
        this.path = path;
    }
}
