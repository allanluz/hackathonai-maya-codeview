package com.sinqia.maya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication
public class MayaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MayaApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
class MayaController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "application", "MAYA Code Review System",
            "version", "1.0.0"
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
            "totalReviews", 156,
            "averageScore", 78.5,
            "criticalIssues", 12,
            "connectionLeaks", 3,
            "topRepositories", new String[]{"sinqia-core", "sinqia-web", "sinqia-api"},
            "lastAnalysis", LocalDateTime.now().minusHours(2)
        );
    }

    @GetMapping("/analysis/summary")
    public Map<String, Object> analysisSummary() {
        return Map.of(
            "codeQuality", Map.of(
                "score", 78.5,
                "grade", "B",
                "trend", "improving"
            ),
            "issues", Map.of(
                "critical", 3,
                "high", 8,
                "medium", 15,
                "low", 24
            ),
            "patterns", Map.of(
                "connectionLeaks", 3,
                "complexityViolations", 7,
                "securityIssues", 2
            ),
            "recommendations", new String[]{
                "Fix connection leaks in UserService.java",
                "Reduce complexity in DataProcessor.analyzeData()",
                "Update deprecated security methods"
            }
        );
    }
}
