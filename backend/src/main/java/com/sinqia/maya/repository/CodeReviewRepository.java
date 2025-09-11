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
 * Reposit�rio para opera��es da entidade CodeReview.
 * 
 * Fornece m�todos para consultas espec�ficas de revis�es de c�digo,
 * incluindo filtros por status, autor, reposit�rio e m�tricas.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long>, 
                                            JpaSpecificationExecutor<CodeReview> {

    /**
     * Buscar revis�o por SHA do commit
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
     * Buscar revis�es por status
     */
    List<CodeReview> findByStatus(CodeReview.ReviewStatus status);

    /**
     * Contar revis�es por status
     */
    long countByStatus(CodeReview.ReviewStatus status);

    /**
     * Buscar revis�es por autor
     */
    Page<CodeReview> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    /**
     * Buscar revis�es por reposit�rio
     */
    Page<CodeReview> findByRepositoryNameContainingIgnoreCase(String repositoryName, Pageable pageable);

    /**
     * Buscar revis�es por projeto
     */
    Page<CodeReview> findByProjectNameContainingIgnoreCase(String projectName, Pageable pageable);

    /**
     * Buscar revis�es com issues cr�ticos
     */
    @Query("SELECT cr FROM CodeReview cr WHERE cr.criticalIssues > 0 ORDER BY cr.criticalIssues DESC")
    List<CodeReview> findReviewsWithCriticalIssues();

    /**
     * Buscar revis�es com vazamentos de conex�o
     */
    @Query("SELECT DISTINCT cr FROM CodeReview cr " +
           "JOIN cr.fileAnalyses fa " +
           "WHERE fa.connectionBalanced = false")
    List<CodeReview> findReviewsWithConnectionLeaks();

    /**
     * Buscar revis�es por per�odo
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cr.createdAt DESC")
    List<CodeReview> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Obter estat�sticas de issues cr�ticos
     */
    @Query("SELECT COALESCE(SUM(cr.criticalIssues), 0) FROM CodeReview cr")
    Long sumCriticalIssues();

    /**
     * Obter m�dia do score de an�lise
     */
    @Query("SELECT AVG(cr.analysisScore) FROM CodeReview cr WHERE cr.analysisScore > 0")
    Double averageAnalysisScore();

    /**
     * Buscar revis�es com score abaixo do limite
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.analysisScore < :scoreThreshold " +
           "ORDER BY cr.analysisScore ASC")
    List<CodeReview> findByScoreBelow(@Param("scoreThreshold") Double scoreThreshold);

    /**
     * Obter top autores com mais revis�es
     */
    @Query("SELECT cr.author, COUNT(cr) as count " +
           "FROM CodeReview cr " +
           "GROUP BY cr.author " +
           "ORDER BY count DESC")
    List<Object[]> findTopAuthorsByReviewCount();

    /**
     * Obter estat�sticas por reposit�rio
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
     * Buscar revis�es pendentes mais antigas
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.status = :status " +
           "ORDER BY cr.createdAt ASC")
    List<CodeReview> findOldestPendingReviews(@Param("status") CodeReview.ReviewStatus status,
                                            Pageable pageable);

    /**
     * Obter revis�es por modelo LLM
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.llmModel = :modelId " +
           "ORDER BY cr.createdAt DESC")
    List<CodeReview> findByLlmModel(@Param("modelId") String modelId);

    /**
     * Verificar se commit j� foi analisado
     */
    boolean existsByCommitShaAndRepositoryName(String commitSha, String repositoryName);

    /**
     * Buscar revis�es com dura��o de an�lise acima do limite
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.analysisDurationMs > :durationMs " +
           "ORDER BY cr.analysisDurationMs DESC")
    List<CodeReview> findByAnalysisDurationAbove(@Param("durationMs") Long durationMs);

    /**
     * Obter estat�sticas de performance por per�odo
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
     * Buscar revis�es similares baseadas no autor e reposit�rio
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
     * Obter trending de qualidade por per�odo
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
     * Buscar revis�es que precisam de reprocessamento
     */
    @Query("SELECT cr FROM CodeReview cr " +
           "WHERE cr.status = 'FAILED' " +
           "OR (cr.status = 'IN_PROGRESS' AND cr.createdAt < :timeoutDate)")
    List<CodeReview> findReviewsNeedingReprocessing(@Param("timeoutDate") LocalDateTime timeoutDate);
    
    // M�todos adicionais para dashboard e m�tricas
    
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
