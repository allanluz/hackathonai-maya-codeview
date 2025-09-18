package com.sinqia.maya.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeAnalysisRequest {
    
    private String code;
    private String language;
    private String model;
    private String prompt;
    
    @JsonProperty("maxTokens")
    private Integer maxTokens;
    
    // Constructors
    public CodeAnalysisRequest() {}
    
    public CodeAnalysisRequest(String code, String language, String model) {
        this.code = code;
        this.language = language;
        this.model = model;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}
