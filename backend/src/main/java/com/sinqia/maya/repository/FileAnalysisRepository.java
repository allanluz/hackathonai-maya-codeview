package com.sinqia.maya.repository;

import com.sinqia.maya.entity.FileAnalysis;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reposit�rio para opera��es da entidade FileAnalysis.
 * 
 * Fornece m�todos para consultas espec�ficas de an�lises de arquivo,
 * incluindo m�tricas de qualidade, complexidade e vazamentos de conex�o.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public interface FileAnalysisRepository extends JpaRepository<FileAnalysis, Long> {

    /**
     * Buscar an�lises por revis�o de c�digo
     */
    List<FileAnalysis> findByCodeReviewIdOrderByFilePathAsc(Long codeReviewId);

    /**
     * Buscar an�lises por linguagem
     */
    List<FileAnalysis> findByLanguage(String language);

    /**
     * Buscar arquivos com vazamentos de conex�o
     */
    @Query("SELECT fa FROM FileAnalysis fa WHERE fa.connectionBalanced = false")
    List<FileAnalysis> findFilesWithConnectionLeaks();

    /**
     * Buscar an�lises por caminho do arquivo
     */
    List<FileAnalysis> findByFilePathContainingIgnoreCase(String filePath);

    /**
     * Buscar an�lises com alta complexidade
     */
    @Query("SELECT fa FROM FileAnalysis fa " +
           "WHERE fa.complexityScore > :threshold " +
           "ORDER BY fa.complexityScore DESC")
    List<FileAnalysis> findByComplexityAbove(@Param("threshold") Double threshold);

    /**
     * Buscar an�lises com score baixo
     */
    @Query("SELECT fa FROM FileAnalysis fa " +
           "WHERE fa.score < :scoreThreshold " +
           "ORDER BY fa.score ASC")
    List<FileAnalysis> findByScoreBelow(@Param("scoreThreshold") Double scoreThreshold);

    /**
     * Obter estat�sticas de conex�es por arquivo
     */
    @Query("SELECT fa.filePath, fa.connectionEmpresta, fa.connectionDevolve, " +
           "fa.connectionBalanced, fa.connectionImbalance " +
           "FROM FileAnalysis fa " +
           "WHERE fa.codeReview.id = :reviewId")
    List<Object[]> getConnectionStatistics(@Param("reviewId") Long reviewId);

    /**
     * Buscar arquivos Java analisados
     */
    @Query("SELECT fa FROM FileAnalysis fa " +
           "WHERE fa.language = 'java' OR fa.filePath LIKE '%.java'")
    List<FileAnalysis> findJavaFiles();

    /**
     * Obter m�dia de complexidade por linguagem
     */
    @Query("SELECT fa.language, AVG(fa.complexityScore) as avgComplexity, COUNT(fa) as fileCount " +
           "FROM FileAnalysis fa " +
           "WHERE fa.complexityScore > 0 " +
           "GROUP BY fa.language " +
           "ORDER BY avgComplexity DESC")
    List<Object[]> getComplexityByLanguage();

    /**
     * Buscar an�lises que usaram IA
     */
    @Query("SELECT fa FROM FileAnalysis fa WHERE fa.aiAnalysisUsed = true")
    List<FileAnalysis> findAnalysesWithAI();

    /**
     * Obter top arquivos com mais issues
     */
    @Query("SELECT fa, SIZE(fa.issues) as issueCount " +
           "FROM FileAnalysis fa " +
           "WHERE SIZE(fa.issues) > 0 " +
           "ORDER BY issueCount DESC")
    List<Object[]> findFilesWithMostIssues(Pageable pageable);

    /**
     * Buscar an�lises por per�odo
     */
    @Query("SELECT fa FROM FileAnalysis fa " +
           "WHERE fa.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY fa.createdAt DESC")
    List<FileAnalysis> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Obter estat�sticas de processamento
     */
    @Query("SELECT AVG(fa.processingTimeMs) as avgProcessingTime, " +
           "MIN(fa.processingTimeMs) as minProcessingTime, " +
           "MAX(fa.processingTimeMs) as maxProcessingTime, " +
           "COUNT(fa) as totalFiles " +
           "FROM FileAnalysis fa " +
           "WHERE fa.processingTimeMs IS NOT NULL")
    Object[] getProcessingStatistics();

    /**
     * Buscar an�lises por classe
     */
    List<FileAnalysis> findByClassNameContainingIgnoreCase(String className);

    /**
     * Obter distribui��o de scores
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN fa.score >= 90 THEN 1 END) as excellent, " +
           "COUNT(CASE WHEN fa.score >= 70 AND fa.score < 90 THEN 1 END) as good, " +
           "COUNT(CASE WHEN fa.score >= 50 AND fa.score < 70 THEN 1 END) as fair, " +
           "COUNT(CASE WHEN fa.score < 50 THEN 1 END) as poor " +
           "FROM FileAnalysis fa " +
           "WHERE fa.score IS NOT NULL")
    Object[] getScoreDistribution();

    /**
     * Buscar arquivos com mudan�as cr�ticas
     */
    @Query("SELECT fa FROM FileAnalysis fa " +
           "WHERE fa.hasTypeChanges = true " +
           "OR fa.hasMethodChanges = true " +
           "OR fa.hasValidationChanges = true")
    List<FileAnalysis> findFilesWithCriticalChanges();

    /**
     * Obter estat�sticas de desequil�brio de conex�es
     */
    @Query("SELECT AVG(fa.connectionImbalance) as avgImbalance, " +
           "MAX(fa.connectionImbalance) as maxImbalance, " +
           "COUNT(CASE WHEN fa.connectionImbalance > 0 THEN 1 END) as imbalancedCount " +
           "FROM FileAnalysis fa " +
           "WHERE fa.connectionImbalance IS NOT NULL")
    Object[] getConnectionImbalanceStatistics();

    /**
     * Buscar an�lises por modelo de IA utilizado
     */
    List<FileAnalysis> findByAiModelUsed(String aiModel);

    /**
     * Obter ranking de arquivos por score
     */
    @Query("SELECT fa.filePath, fa.score, fa.complexityScore, " +
           "fa.connectionBalanced, SIZE(fa.issues) as issueCount " +
           "FROM FileAnalysis fa " +
           "WHERE fa.codeReview.id = :reviewId " +
           "ORDER BY fa.score DESC")
    List<Object[]> getFileRanking(@Param("reviewId") Long reviewId);

    /**
     * Buscar arquivos similares por nome da classe
     */
    @Query("SELECT fa FROM FileAnalysis fa " +
           "WHERE fa.className = :className " +
           "AND fa.id != :excludeId " +
           "ORDER BY fa.createdAt DESC")
    List<FileAnalysis> findSimilarFilesByClassName(@Param("className") String className,
                                                  @Param("excludeId") Long excludeId,
                                                  Pageable pageable);

    /**
     * Obter evolu��o da qualidade de um arquivo
     */
    @Query("SELECT fa.score, fa.complexityScore, fa.createdAt " +
           "FROM FileAnalysis fa " +
           "WHERE fa.filePath = :filePath " +
           "AND fa.codeReview.repositoryName = :repositoryName " +
           "ORDER BY fa.createdAt ASC")
    List<Object[]> getFileQualityEvolution(@Param("filePath") String filePath,
                                         @Param("repositoryName") String repositoryName);

    /**
     * Contar arquivos analisados por reposit�rio
     */
    @Query("SELECT fa.codeReview.repositoryName, COUNT(fa) " +
           "FROM FileAnalysis fa " +
           "GROUP BY fa.codeReview.repositoryName " +
           "ORDER BY COUNT(fa) DESC")
    List<Object[]> countFilesByRepository();
}
