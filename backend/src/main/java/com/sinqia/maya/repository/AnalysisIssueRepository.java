package com.sinqia.maya.repository;

import com.sinqia.maya.entity.AnalysisIssue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para operações da entidade AnalysisIssue.
 * 
 * Fornece métodos para consultas específicas de issues encontrados,
 * incluindo filtros por severidade, tipo e estatísticas.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public interface AnalysisIssueRepository extends JpaRepository<AnalysisIssue, Long> {

    /**
     * Buscar issues por análise de arquivo
     */
    List<AnalysisIssue> findByFileAnalysisIdOrderBySeverityDescLineNumberAsc(Long fileAnalysisId);

    /**
     * Buscar issues por severidade
     */
    List<AnalysisIssue> findBySeverity(AnalysisIssue.IssueSeverity severity);

    /**
     * Buscar issues por tipo
     */
    List<AnalysisIssue> findByType(AnalysisIssue.IssueType type);

    /**
     * Contar issues por severidade
     */
    long countBySeverity(AnalysisIssue.IssueSeverity severity);

    /**
     * Contar issues por tipo
     */
    long countByType(AnalysisIssue.IssueType type);

    /**
     * Buscar issues críticos de vazamento de conexão
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.type = 'CONNECTION_LEAK' " +
           "AND ai.severity = 'CRITICAL' " +
           "ORDER BY ai.createdAt DESC")
    List<AnalysisIssue> findCriticalConnectionLeaks();

    /**
     * Buscar issues de segurança
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.type IN ('SECURITY_ISSUE', 'SQL_INJECTION', 'SENSITIVE_DATA') " +
           "ORDER BY ai.severity DESC, ai.createdAt DESC")
    List<AnalysisIssue> findSecurityIssues();

    /**
     * Obter estatísticas de issues por revisão
     */
    @Query("SELECT ai.severity, ai.type, COUNT(ai) " +
           "FROM AnalysisIssue ai " +
           "WHERE ai.fileAnalysis.codeReview.id = :reviewId " +
           "GROUP BY ai.severity, ai.type " +
           "ORDER BY ai.severity DESC")
    List<Object[]> getIssueStatisticsByReview(@Param("reviewId") Long reviewId);

    /**
     * Buscar top issues mais comuns
     */
    @Query("SELECT ai.type, ai.title, COUNT(ai) as issueCount " +
           "FROM AnalysisIssue ai " +
           "GROUP BY ai.type, ai.title " +
           "ORDER BY issueCount DESC")
    List<Object[]> findMostCommonIssues(Pageable pageable);

    /**
     * Buscar issues por linha específica
     */
    List<AnalysisIssue> findByLineNumber(Integer lineNumber);

    /**
     * Buscar issues que podem ser corrigidos automaticamente
     */
    @Query("SELECT ai FROM AnalysisIssue ai WHERE ai.autoFixable = true")
    List<AnalysisIssue> findAutoFixableIssues();

    /**
     * Buscar issues por período
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ai.createdAt DESC")
    List<AnalysisIssue> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Obter distribuição de severidade
     */
    @Query("SELECT ai.severity, COUNT(ai) " +
           "FROM AnalysisIssue ai " +
           "GROUP BY ai.severity " +
           "ORDER BY ai.severity DESC")
    List<Object[]> getSeverityDistribution();

    /**
     * Obter distribuição de tipos
     */
    @Query("SELECT ai.type, COUNT(ai) " +
           "FROM AnalysisIssue ai " +
           "GROUP BY ai.type " +
           "ORDER BY COUNT(ai) DESC")
    List<Object[]> getTypeDistribution();

    /**
     * Buscar issues por repositório
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.fileAnalysis.codeReview.repositoryName = :repositoryName " +
           "ORDER BY ai.severity DESC, ai.createdAt DESC")
    List<AnalysisIssue> findByRepository(@Param("repositoryName") String repositoryName);

    /**
     * Buscar issues por autor
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.fileAnalysis.codeReview.author = :author " +
           "ORDER BY ai.severity DESC, ai.createdAt DESC")
    List<AnalysisIssue> findByAuthor(@Param("author") String author);

    /**
     * Obter trending de issues por período
     */
    @Query("SELECT DATE(ai.createdAt) as issueDate, " +
           "ai.severity, " +
           "COUNT(ai) as issueCount " +
           "FROM AnalysisIssue ai " +
           "WHERE ai.createdAt >= :startDate " +
           "GROUP BY DATE(ai.createdAt), ai.severity " +
           "ORDER BY issueDate ASC, ai.severity DESC")
    List<Object[]> getIssueTrend(@Param("startDate") LocalDateTime startDate);

    /**
     * Buscar issues de alta complexidade
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.type = 'COMPLEXITY' " +
           "ORDER BY ai.severity DESC, ai.lineNumber ASC")
    List<AnalysisIssue> findComplexityIssues();

    /**
     * Obter issues por arquivo específico
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.fileAnalysis.filePath = :filePath " +
           "ORDER BY ai.severity DESC, ai.lineNumber ASC")
    List<AnalysisIssue> findByFilePath(@Param("filePath") String filePath);

    /**
     * Buscar issues com sugestões
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.suggestion IS NOT NULL " +
           "AND LENGTH(ai.suggestion) > 0")
    List<AnalysisIssue> findIssuesWithSuggestions();

    /**
     * Obter estatísticas de resolução por tipo
     */
    @Query("SELECT ai.type, " +
           "COUNT(CASE WHEN ai.autoFixable = true THEN 1 END) as autoFixableCount, " +
           "COUNT(ai) as totalCount " +
           "FROM AnalysisIssue ai " +
           "GROUP BY ai.type " +
           "ORDER BY totalCount DESC")
    List<Object[]> getResolutionStatistics();

    /**
     * Buscar issues similares
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.type = :type " +
           "AND ai.title = :title " +
           "AND ai.id != :excludeId " +
           "ORDER BY ai.createdAt DESC")
    List<AnalysisIssue> findSimilarIssues(@Param("type") AnalysisIssue.IssueType type,
                                        @Param("title") String title,
                                        @Param("excludeId") Long excludeId,
                                        Pageable pageable);

    /**
     * Obter hotspots de issues (arquivos com mais issues)
     */
    @Query("SELECT ai.fileAnalysis.filePath, " +
           "COUNT(ai) as issueCount, " +
           "COUNT(CASE WHEN ai.severity = 'CRITICAL' THEN 1 END) as criticalCount " +
           "FROM AnalysisIssue ai " +
           "GROUP BY ai.fileAnalysis.filePath " +
           "ORDER BY issueCount DESC")
    List<Object[]> getIssueHotspots(Pageable pageable);

    /**
     * Buscar issues recentes por repositório
     */
    @Query("SELECT ai FROM AnalysisIssue ai " +
           "WHERE ai.fileAnalysis.codeReview.repositoryName = :repositoryName " +
           "AND ai.createdAt >= :sinceDate " +
           "ORDER BY ai.createdAt DESC")
    List<AnalysisIssue> findRecentIssuesByRepository(@Param("repositoryName") String repositoryName,
                                                    @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Obter métricas de qualidade por autor
     */
    @Query("SELECT ai.fileAnalysis.codeReview.author, " +
           "COUNT(ai) as totalIssues, " +
           "COUNT(CASE WHEN ai.severity = 'CRITICAL' THEN 1 END) as criticalIssues, " +
           "COUNT(CASE WHEN ai.type = 'CONNECTION_LEAK' THEN 1 END) as connectionLeaks " +
           "FROM AnalysisIssue ai " +
           "GROUP BY ai.fileAnalysis.codeReview.author " +
           "ORDER BY totalIssues DESC")
    List<Object[]> getQualityMetricsByAuthor();

    /**
     * Buscar issues por regra violada
     */
    List<AnalysisIssue> findByRuleViolated(String ruleViolated);
}
