package com.sinqia.maya.dto.everai;

import java.util.List;

public class EverAiModelsResponse {
    
    private String status;
    private List<String> message;
    
    public EverAiModelsResponse() {}
    
    public EverAiModelsResponse(String status, List<String> message) {
        this.status = status;
        this.message = message;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getMessage() { return message; }
    public void setMessage(List<String> message) { this.message = message; }
}