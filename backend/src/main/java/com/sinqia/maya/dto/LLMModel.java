package com.sinqia.maya.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LLMModel {
    
    private String id;
    private String name;
    private String description;
    private String provider;
    
    @JsonProperty("maxTokens")
    private Integer maxTokens;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    // Constructors
    public LLMModel() {}
    
    public LLMModel(String id, String name, String description, String provider, Integer maxTokens, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.provider = provider;
        this.maxTokens = maxTokens;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "LLMModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", provider='" + provider + '\'' +
                ", maxTokens=" + maxTokens +
                ", isActive=" + isActive +
                '}';
    }
}
