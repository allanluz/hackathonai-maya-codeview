package com.sinqia.maya.dto.everai;

import java.util.List;
import java.util.Map;

public class EverAiChatRequest {
    
    private String model;
    private List<ChatMessage> messages;
    private String llmFamily;
    private Double temperature;
    private Integer maxOutputTokens;
    private KnowledgeBase knowledgeBase;
    
    public static class ChatMessage {
        private String role;
        private String content;
        
        public ChatMessage() {}
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class KnowledgeBase {
        private List<String> knowledgeBase;
        private Double maxRelevance;
        private Integer results;
        private List<String> theme;
        private String version;
        
        public List<String> getKnowledgeBase() { return knowledgeBase; }
        public void setKnowledgeBase(List<String> knowledgeBase) { this.knowledgeBase = knowledgeBase; }
        public Double getMaxRelevance() { return maxRelevance; }
        public void setMaxRelevance(Double maxRelevance) { this.maxRelevance = maxRelevance; }
        public Integer getResults() { return results; }
        public void setResults(Integer results) { this.results = results; }
        public List<String> getTheme() { return theme; }
        public void setTheme(List<String> theme) { this.theme = theme; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
    
    // Getters and Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    public String getLlmFamily() { return llmFamily; }
    public void setLlmFamily(String llmFamily) { this.llmFamily = llmFamily; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxOutputTokens() { return maxOutputTokens; }
    public void setMaxOutputTokens(Integer maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
    public KnowledgeBase getKnowledgeBase() { return knowledgeBase; }
    public void setKnowledgeBase(KnowledgeBase knowledgeBase) { this.knowledgeBase = knowledgeBase; }
}