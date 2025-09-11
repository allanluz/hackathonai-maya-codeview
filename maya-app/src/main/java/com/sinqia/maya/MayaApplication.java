package com.sinqia.maya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MayaApplication {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  MAYA Code Review System - Starting...  ");
        System.out.println("===========================================");
        SpringApplication.run(MayaApplication.class, args);
        System.out.println("System started successfully!");
        System.out.println("Access: http://localhost:8080/api/health");
        System.out.println("Dashboard: http://localhost:8080/api/dashboard");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "MAYA Code Review System");
        status.put("version", "1.0.0");
        status.put("description", "Sistema de Code Review inteligente com IA");
        return status;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", 15);
        stats.put("reviewsThisMonth", 8);
        stats.put("averageScore", 8.5);
        stats.put("issuesFound", 42);
        stats.put("issuesResolved", 38);
        
        // Recent reviews
        List<Map<String, Object>> recentReviews = new ArrayList<>();
        
        Map<String, Object> review1 = new HashMap<>();
        review1.put("id", 1);
        review1.put("title", "Feature: User Authentication");
        review1.put("author", "dev1@sinqia.com");
        review1.put("score", 9.2);
        review1.put("status", "APPROVED");
        review1.put("createdAt", LocalDateTime.now().minusDays(1));
        recentReviews.add(review1);
        
        Map<String, Object> review2 = new HashMap<>();
        review2.put("id", 2);
        review2.put("title", "Fix: Database Connection Pool");
        review2.put("author", "dev2@sinqia.com");
        review2.put("score", 8.7);
        review2.put("status", "PENDING");
        review2.put("createdAt", LocalDateTime.now().minusDays(2));
        recentReviews.add(review2);
        
        Map<String, Object> review3 = new HashMap<>();
        review3.put("id", 3);
        review3.put("title", "Enhancement: API Performance");
        review3.put("author", "dev3@sinqia.com");
        review3.put("score", 7.8);
        review3.put("status", "NEEDS_REVISION");
        review3.put("createdAt", LocalDateTime.now().minusDays(3));
        recentReviews.add(review3);
        
        dashboard.put("statistics", stats);
        dashboard.put("recentReviews", recentReviews);
        dashboard.put("timestamp", LocalDateTime.now());
        
        return dashboard;
    }

    @GetMapping("/analysis/summary")
    public Map<String, Object> analysisSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Code quality metrics
        Map<String, Object> codeQuality = new HashMap<>();
        codeQuality.put("overallScore", 8.3);
        codeQuality.put("maintainabilityIndex", 85);
        codeQuality.put("technicalDebt", "Low");
        codeQuality.put("codeComplexity", "Medium");
        
        // Issue distribution
        Map<String, Object> issues = new HashMap<>();
        issues.put("critical", 2);
        issues.put("high", 5);
        issues.put("medium", 15);
        issues.put("low", 20);
        issues.put("total", 42);
        
        // AI insights
        List<String> insights = new ArrayList<>();
        insights.add("Code documentation has improved by 25% this month");
        insights.add("Cyclomatic complexity is within acceptable limits");
        insights.add("Consider refactoring the UserService class");
        insights.add("Unit test coverage increased to 78%");
        
        summary.put("codeQuality", codeQuality);
        summary.put("issueDistribution", issues);
        summary.put("aiInsights", insights);
        summary.put("lastAnalysis", LocalDateTime.now().minusHours(2));
        
        return summary;
    }

    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> welcome = new HashMap<>();
        welcome.put("message", "Welcome to MAYA Code Review System");
        welcome.put("description", "Sistema inteligente de revisao de codigo com IA");
        welcome.put("version", "1.0.0");
        welcome.put("company", "Sinqia");
        welcome.put("timestamp", LocalDateTime.now());
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "/api/health");
        endpoints.put("dashboard", "/api/dashboard");
        endpoints.put("analysis", "/api/analysis/summary");
        welcome.put("availableEndpoints", endpoints);
        
        return welcome;
    }
}
