package com.sinqia.maya.dto.everai;

import java.util.List;
import java.util.Map;

public class EverAiChatResponse {
    
    private ChatMessage message;
    private Parameters parameters;
    
    public static class ChatMessage {
        private String content;
        private String role;
        
        public ChatMessage() {}
        
        public ChatMessage(String content, String role) {
            this.content = content;
            this.role = role;
        }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    public static class Parameters {
        private Object knowledgeBase;
        private String llmFamily;
        private List<ChatMessage> messages;
        private String model;
        private Double temperature;
        
        public Object getKnowledgeBase() { return knowledgeBase; }
        public void setKnowledgeBase(Object knowledgeBase) { this.knowledgeBase = knowledgeBase; }
        public String getLlmFamily() { return llmFamily; }
        public void setLlmFamily(String llmFamily) { this.llmFamily = llmFamily; }
        public List<ChatMessage> getMessages() { return messages; }
        public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
    }
    
    // Getters and Setters
    public ChatMessage getMessage() { return message; }
    public void setMessage(ChatMessage message) { this.message = message; }
    public Parameters getParameters() { return parameters; }
    public void setParameters(Parameters parameters) { this.parameters = parameters; }
}