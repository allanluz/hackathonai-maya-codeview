package com.sinqia.maya.repository;

import com.sinqia.maya.entity.CodeReview;
import com.sinqia.maya.repository.impl.CodeReviewRepositoryImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações da entidade CodeReview.
 * 
 * Fornece métodos para consultas específicas de revisões de código,
 * incluindo filtros por status, autor, repositório e métricas.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long>, 
                                            JpaSpecificationExecutor<CodeReview> {

    /**
     * Buscar revisão por SHA do commit
     */
    Optional<CodeReview> findByCommitSha(String commitSha);

    /**
     * Find all reviews ordered by creation date descending
     */
    Page<CodeReview> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Count reviews created after a specific date
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Count reviews by status created after a specific date
     */
    long countByStatusAndCreatedAtAfter(CodeReview.ReviewStatus status, LocalDateTime date);

    /**
     * Find reviews by author ordered by creation date descending
     */
    Page<CodeReview> findByAuthorOrderByCreatedAtDesc(String author, Pageable pageable);

    /**
     * Buscar revisões por status
     */
    List<CodeReview> findByStatus(CodeReview.ReviewStatus status);

    /**
     * Contar revisões por status
     */
    long countByStatus(CodeReview.ReviewStatus status);

    /**
     * Buscar revisões por autor
     */
    Page<CodeReview> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    /**
     * Buscar revisões por repositório
     */
    Page<CodeReview> findByRepositoryNameContainingIgnoreCase(String repositoryName, Pageable pageable);

    /**
     * Buscar revisões por projeto
     */
    Page<CodeReview> findByProjectNameContainingIgnoreCase(String projectName, Pageable pageable);

    /**
     * Buscar revisões com issues críticos
     */
    @Query("SELECT cr FROM CodeReview cr WHERE cr.criticalIssues > 0 ORDER BY cr.criticalIssues DESC")
    List<CodeReview> findReviewsWithCriticalIssues();

    /**
     * Buscar revisões com vazamentos de conexão
     */
    @Query("SELECT DISTINCT cr FROM CodeReview cr " +
           "JOIN cr.fileAnalyses fa " +
           "WHERE fa.connectionBalanced = false")
    List<CodeReview> findReviewsWithConnectionLeaks();

    /**
     * Buscar revisões por período
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cr.createdAt DESC")
    List<CodeReview> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Obter estatísticas de issues críticos
     */
    @Query("SELECT COALESCE(SUM(cr.criticalIssues), 0) FROM CodeReview cr")
    Long sumCriticalIssues();

    /**
     * Obter média do score de análise
     */
    @Query("SELECT AVG(cr.analysisScore) FROM CodeReview cr WHERE cr.analysisScore > 0")
    Double averageAnalysisScore();

    /**
     * Buscar revisões com score abaixo do limite
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.analysisScore < :scoreThreshold " +
           "ORDER BY cr.analysisScore ASC")
    List<CodeReview> findByScoreBelow(@Param("scoreThreshold") Double scoreThreshold);

    /**
     * Obter top autores com mais revisões
     */
    @Query("SELECT cr.author, COUNT(cr) as count " +
           "FROM CodeReview cr " +
           "GROUP BY cr.author " +
           "ORDER BY count DESC")
    List<Object[]> findTopAuthorsByReviewCount();

    /**
     * Obter estatísticas por repositório
     */
    @Query("SELECT cr.repositoryName, " +
           "COUNT(cr) as totalReviews, " +
           "AVG(cr.analysisScore) as avgScore, " +
           "SUM(cr.criticalIssues) as totalCritical " +
           "FROM CodeReview cr " +
           "GROUP BY cr.repositoryName " +
           "ORDER BY totalReviews DESC")
    List<Object[]> getRepositoryStatistics();

    /**
     * Buscar revisões pendentes mais antigas
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.status = :status " +
           "ORDER BY cr.createdAt ASC")
    List<CodeReview> findOldestPendingReviews(@Param("status") CodeReview.ReviewStatus status,
                                            Pageable pageable);

    /**
     * Obter revisões por modelo LLM
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.llmModel = :modelId " +
           "ORDER BY cr.createdAt DESC")
    List<CodeReview> findByLlmModel(@Param("modelId") String modelId);

    /**
     * Verificar se commit já foi analisado
     */
    boolean existsByCommitShaAndRepositoryName(String commitSha, String repositoryName);

    /**
     * Buscar revisões com duração de análise acima do limite
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.analysisDurationMs > :durationMs " +
           "ORDER BY cr.analysisDurationMs DESC")
    List<CodeReview> findByAnalysisDurationAbove(@Param("durationMs") Long durationMs);

    /**
     * Obter estatísticas de performance por período
     */
    @Query("SELECT DATE(cr.createdAt) as analysisDate, " +
           "COUNT(cr) as totalReviews, " +
           "AVG(cr.analysisDurationMs) as avgDuration, " +
           "AVG(cr.analysisScore) as avgScore " +
           "FROM CodeReview cr " +
           "WHERE cr.createdAt >= :startDate " +
           "GROUP BY DATE(cr.createdAt) " +
           "ORDER BY analysisDate DESC")
    List<Object[]> getPerformanceStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * Buscar revisões similares baseadas no autor e repositório
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.author = :author " +
           "AND cr.repositoryName = :repositoryName " +
           "AND cr.id != :excludeId " +
           "ORDER BY cr.createdAt DESC")
    List<CodeReview> findSimilarReviews(@Param("author") String author,
                                      @Param("repositoryName") String repositoryName,
                                      @Param("excludeId") Long excludeId,
                                      Pageable pageable);

    /**
     * Obter trending de qualidade por período
     */
    @Query("SELECT DATE(cr.createdAt) as analysisDate, " +
           "AVG(cr.analysisScore) as avgScore, " +
           "COUNT(CASE WHEN cr.criticalIssues > 0 THEN 1 END) as criticalCount, " +
           "COUNT(cr) as totalCount " +
           "FROM CodeReview cr " +
           "WHERE cr.createdAt >= :startDate " +
           "GROUP BY DATE(cr.createdAt) " +
           "ORDER BY analysisDate ASC")
    List<Object[]> getQualityTrend(@Param("startDate") LocalDateTime startDate);

    /**
     * Buscar revisões que precisam de reprocessamento
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.status = 'FAILED' " +
           "OR (cr.status = 'IN_PROGRESS' AND cr.createdAt < :timeoutDate)")
    List<CodeReview> findReviewsNeedingReprocessing(@Param("timeoutDate") LocalDateTime timeoutDate);
    
    // Métodos adicionais para dashboard e métricas
    
    @Query("SELECT AVG(cr.analysisScore) FROM CodeReview cr WHERE cr.createdAt >= :cutoffDate")
    Double findAverageScoreAfter(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(i) FROM AnalysisIssue i JOIN i.fileAnalysis fa JOIN fa.codeReview cr WHERE cr.createdAt >= :cutoffDate AND i.severity = 'CRITICAL'")
    Long countCriticalIssuesAfter(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT cr.repositoryName, COUNT(cr) FROM CodeReview cr WHERE cr.createdAt >= :cutoffDate GROUP BY cr.repositoryName ORDER BY COUNT(cr) DESC")
    List<Object[]> findTopRepositoriesByReviewCount(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT cr.author, COUNT(cr) FROM CodeReview cr WHERE cr.createdAt >= :cutoffDate GROUP BY cr.author ORDER BY COUNT(cr) DESC")
    List<Object[]> findTopAuthorsByReviewCount(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT cr.repositoryName, COUNT(cr), AVG(cr.analysisScore), " +
           "SUM(CASE WHEN i.severity = 'CRITICAL' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN cr.status = 'COMPLETED' THEN 1 ELSE 0 END) " +
           "FROM CodeReview cr LEFT JOIN cr.fileAnalyses fa LEFT JOIN fa.issues i " +
           "WHERE cr.createdAt >= :cutoffDate " +
           "GROUP BY cr.repositoryName")
    List<Object[]> findRepositoryMetrics(@Param("cutoffDate") LocalDateTime cutoffDate);
}
