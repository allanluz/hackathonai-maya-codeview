package com.sinqia.maya.controller;

import com.sinqia.maya.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller para dashboard e métricas do sistema MAYA
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Obter overview geral do sistema
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverview> getOverview(
            @RequestParam(required = false, defaultValue = "30") int days) {
        
        log.debug("Gerando overview do dashboard para {} dias", days);
        
        DashboardOverview overview = dashboardService.getOverview(days);
        return ResponseEntity.ok(overview);
    }

    /**
     * Obter métricas de qualidade de código
     */
    @GetMapping("/quality-metrics")
    public ResponseEntity<QualityMetrics> getQualityMetrics(
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false) Long repositoryId) {
        
        log.debug("Gerando métricas de qualidade para {} dias, repo: {}", days, repositoryId);
        
        QualityMetrics metrics = dashboardService.getQualityMetrics(days, repositoryId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Obter tendências temporais
     */
    @GetMapping("/trends")
    public ResponseEntity<TrendAnalysis> getTrends(
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false, defaultValue = "DAILY") String period) {
        
        log.debug("Gerando análise de tendências para {} dias, período: {}", days, period);
        
        TrendAnalysis trends = dashboardService.getTrends(days, period);
        return ResponseEntity.ok(trends);
    }

    /**
     * Obter ranking de repositórios
     */
    @GetMapping("/repository-ranking")
    public ResponseEntity<List<RepositoryRanking>> getRepositoryRanking(
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        log.debug("Gerando ranking de repositórios para {} dias, limit: {}", days, limit);
        
        List<RepositoryRanking> ranking = dashboardService.getRepositoryRanking(days, limit);
        return ResponseEntity.ok(ranking);
    }

    /**
     * Obter ranking de desenvolvedores
     */
    @GetMapping("/developer-ranking")
    public ResponseEntity<List<DeveloperRanking>> getDeveloperRanking(
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        log.debug("Gerando ranking de desenvolvedores para {} dias, limit: {}", days, limit);
        
        List<DeveloperRanking> ranking = dashboardService.getDeveloperRanking(days, limit);
        return ResponseEntity.ok(ranking);
    }

    /**
     * Obter estatísticas de tipos de problemas
     */
    @GetMapping("/issue-statistics")
    public ResponseEntity<IssueStatistics> getIssueStatistics(
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false) Long repositoryId) {
        
        log.debug("Gerando estatísticas de problemas para {} dias, repo: {}", days, repositoryId);
        
        IssueStatistics statistics = dashboardService.getIssueStatistics(days, repositoryId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Obter métricas de performance do sistema
     */
    @GetMapping("/performance-metrics")
    public ResponseEntity<PerformanceMetrics> getPerformanceMetrics(
            @RequestParam(required = false, defaultValue = "7") int days) {
        
        log.debug("Gerando métricas de performance para {} dias", days);
        
        PerformanceMetrics metrics = dashboardService.getPerformanceMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Obter alertas ativos
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<SystemAlert>> getActiveAlerts() {
        log.debug("Buscando alertas ativos");
        
        List<SystemAlert> alerts = dashboardService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Obter dados para gráficos
     */
    @GetMapping("/charts/{chartType}")
    public ResponseEntity<ChartData> getChartData(
            @PathVariable String chartType,
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false) Long repositoryId) {
        
        log.debug("Gerando dados do gráfico: {} para {} dias", chartType, days);
        
        ChartData chartData = dashboardService.getChartData(chartType, days, repositoryId);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Exportar dados do dashboard
     */
    @PostMapping("/export")
    public ResponseEntity<ExportResponse> exportDashboard(@RequestBody ExportRequest request) {
        log.info("Exportando dados do dashboard: {}", request.format());
        
        try {
            String exportedData = dashboardService.exportDashboard(request);
            
            return ResponseEntity.ok(new ExportResponse(
                    true,
                    "Dados exportados com sucesso",
                    exportedData,
                    request.format()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao exportar dashboard: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new ExportResponse(false, e.getMessage(), null, null));
        }
    }

    // DTOs para respostas

    public record DashboardOverview(
            long totalReviews,
            long activeRepositories,
            double averageQualityScore,
            long criticalIssues,
            long connectionLeaks,
            double completionRate,
            LocalDateTime lastUpdate,
            List<QuickStat> quickStats
    ) {}

    public record QuickStat(
            String name,
            Object value,
            String unit,
            String trend,
            double changePercent
    ) {}

    public record QualityMetrics(
            double averageScore,
            double medianScore,
            double scoreDistribution,
            long totalFiles,
            long analyzedFiles,
            Map<String, Long> issuesByType,
            Map<String, Double> scoresByRepository,
            List<QualityTrend> trends
    ) {}

    public record QualityTrend(
            LocalDateTime date,
            double score,
            long issues,
            long reviews
    ) {}

    public record TrendAnalysis(
            String period,
            List<TrendPoint> reviewsTrend,
            List<TrendPoint> qualityTrend,
            List<TrendPoint> issuesTrend,
            Map<String, Object> insights
    ) {}

    public record TrendPoint(
            LocalDateTime timestamp,
            double value,
            String label
    ) {}

    public record RepositoryRanking(
            Long repositoryId,
            String repositoryName,
            double averageScore,
            long totalReviews,
            long criticalIssues,
            double trend,
            String status
    ) {}

    public record DeveloperRanking(
            String developer,
            double averageScore,
            long totalReviews,
            long criticalIssues,
            double improvement,
            String level
    ) {}

    public record IssueStatistics(
            Map<String, Long> issuesByType,
            Map<String, Long> issuesBySeverity,
            Map<String, Double> resolutionRate,
            List<TopIssue> topIssues,
            Map<String, Long> issuesByFile
    ) {}

    public record TopIssue(
            String type,
            String description,
            long count,
            String severity,
            double impact
    ) {}

    public record PerformanceMetrics(
            double averageAnalysisTime,
            double systemUptime,
            long totalMemoryUsage,
            double cpuUsage,
            long requestCount,
            double errorRate,
            List<PerformancePoint> timeline
    ) {}

    public record PerformancePoint(
            LocalDateTime timestamp,
            double analysisTime,
            double memoryUsage,
            double cpuUsage
    ) {}

    public record SystemAlert(
            String id,
            String type,
            String severity,
            String message,
            String details,
            LocalDateTime timestamp,
            boolean resolved
    ) {}

    public record ChartData(
            String chartType,
            List<String> labels,
            List<DataSeries> datasets,
            Map<String, Object> options
    ) {}

    public record DataSeries(
            String label,
            List<Object> data,
            String backgroundColor,
            String borderColor,
            Map<String, Object> styling
    ) {}

    public record ExportRequest(
            String format, // PDF, CSV, EXCEL, JSON
            List<String> sections,
            int days,
            Long repositoryId,
            Map<String, Object> options
    ) {}

    public record ExportResponse(
            boolean success,
            String message,
            String data,
            String format
    ) {}
}
