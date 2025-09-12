package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade para métricas do dashboard
 */
@Entity
@Table(name = "dashboard_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "metric_value")
    private Double metricValue;

    @Column(name = "metric_type")
    @Enumerated(EnumType.STRING)
    private MetricType metricType;

    @Column(name = "repository_id")
    private Long repositoryId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "author")
    private String author;

    @Column(name = "time_period")
    private String timePeriod; // DAILY, WEEKLY, MONTHLY

    @CreationTimestamp
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "additional_data", length = 1000)
    private String additionalData; // JSON para dados extras

    public enum MetricType {
        REVIEW_COUNT,
        AVERAGE_SCORE,
        CRITICAL_ISSUES,
        ANALYSIS_TIME,
        CONNECTION_LEAKS,
        CODE_QUALITY,
        COMPLETION_RATE,
        ERROR_RATE,
        PERFORMANCE
    }
}
