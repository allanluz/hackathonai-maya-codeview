package com.sinqia.maya.dto;

import java.util.List;

public class ReportGenerationResponse {
    
    private String report;
    private List<String> recommendations;
    
    // Constructors
    public ReportGenerationResponse() {}
    
    // Getters and Setters
    public String getReport() {
        return report;
    }
    
    public void setReport(String report) {
        this.report = report;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
