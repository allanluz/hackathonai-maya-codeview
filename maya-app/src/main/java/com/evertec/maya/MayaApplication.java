package com.evertec.maya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MAYA Code Review System - Evertec
 * Simplified system for MAYA architecture demonstration
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class MayaApplication {

    public static void main(String[] args) {
        System.out.println("?? Starting MAYA System - Evertec");
        System.out.println("?? Dashboard available at: http://localhost:8080/api/dashboard");
        System.out.println("?? Analysis available at: http://localhost:8080/api/analysis/summary");
        SpringApplication.run(MayaApplication.class, args);
    }

    /**
     * System health endpoint
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "MAYA Code Review - Evertec");
        health.put("version", "1.0.0");
        return health;
    }

    /**
     * Main dashboard with system metrics
     */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Simulated metrics for demonstration
        dashboard.put("totalReviews", 127);
        dashboard.put("activeReviews", 8);
        dashboard.put("completedToday", 5);
        dashboard.put("averageScore", 8.3);
        dashboard.put("connectionLeaksDetected", 3);
        dashboard.put("evertecStandardsCompliance", 94.5);
        dashboard.put("lastUpdate", LocalDateTime.now());
        
        // Services status
        Map<String, String> services = new HashMap<>();
        services.put("database", "CONNECTED");
        services.put("tfs", "CONNECTED");
        services.put("evertecAI", "CONNECTED");
        dashboard.put("services", services);
        
        return dashboard;
    }

    /**
     * Code analysis summary
     */
    @GetMapping("/analysis/summary")
    public Map<String, Object> analysisSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Analysis summary
        summary.put("totalFiles", 234);
        summary.put("filesAnalyzed", 187);
        summary.put("issuesFound", 23);
        summary.put("criticalIssues", 2);
        summary.put("connectionLeaks", 1);
        summary.put("evertecPatternViolations", 4);
        
        // Top issues
        Map<String, Integer> topIssues = new HashMap<>();
        topIssues.put("Connection not closed", 5);
        topIssues.put("Evertec naming pattern", 8);
        topIssues.put("Exception handling", 3);
        topIssues.put("Code complexity", 7);
        summary.put("topIssues", topIssues);
        
        summary.put("generatedAt", LocalDateTime.now());
        
        return summary;
    }
}
