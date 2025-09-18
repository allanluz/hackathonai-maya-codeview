package com.sinqia.maya.dto;

import java.util.List;

public class CodeAnalysisResponse {
    
    private String analysis;
    private List<AnalysisIssue> issues;
    private Integer score;
    private String summary;
    private String status;
    
    // Constructors
    public CodeAnalysisResponse() {}
    
    // Getters and Setters
    public String getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    
    public List<AnalysisIssue> getIssues() {
        return issues;
    }
    
    public void setIssues(List<AnalysisIssue> issues) {
        this.issues = issues;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public static class AnalysisIssue {
        private String severity;
        private String type;
        private String message;
        private Integer line;
        private String suggestion;
        
        // Constructors
        public AnalysisIssue() {}
        
        // Getters and Setters
        public String getSeverity() {
            return severity;
        }
        
        public void setSeverity(String severity) {
            this.severity = severity;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public Integer getLine() {
            return line;
        }
        
        public void setLine(Integer line) {
            this.line = line;
        }
        
        public String getSuggestion() {
            return suggestion;
        }
        
        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }
    }
}
