package com.sinqia.maya.repository;

import com.sinqia.maya.entity.AuxiliaryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações da entidade AuxiliaryFile.
 * 
 * Fornece métodos para gerenciamento de arquivos auxiliares do sistema MAYA,
 * incluindo documentação, prompts, templates e padrões de código.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public interface AuxiliaryFileRepository extends JpaRepository<AuxiliaryFile, Long> {

    /**
     * Buscar arquivo por nome
     */
    Optional<AuxiliaryFile> findByName(String name);

    /**
     * Buscar arquivo por tipo e nome
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType = :fileType " +
           "AND af.name = :name " +
           "AND af.isActive = true")
    Optional<AuxiliaryFile> findByTypeAndName(@Param("fileType") String fileType, @Param("name") String name);

    /**
     * Buscar arquivos por tipo
     */
    List<AuxiliaryFile> findByFileTypeOrderByNameAsc(AuxiliaryFile.FileType fileType);

    /**
     * Buscar arquivos ativos
     */
    @Query("SELECT af FROM AuxiliaryFile af WHERE af.isActive = true ORDER BY af.name ASC")
    List<AuxiliaryFile> findActiveFiles();

    /**
     * Buscar arquivos por tag
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE LOWER(af.tags) LIKE LOWER(CONCAT('%', :tag, '%')) " +
           "AND af.isActive = true " +
           "ORDER BY af.name ASC")
    List<AuxiliaryFile> findByTag(@Param("tag") String tag);

    /**
     * Buscar arquivos mais utilizados
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.usageCount > 0 " +
           "ORDER BY af.usageCount DESC, af.lastUsedAt DESC")
    List<AuxiliaryFile> findMostUsed();

    /**
     * Buscar prompts para IA
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType = 'PROMPT' " +
           "AND af.isActive = true " +
           "ORDER BY af.usageCount DESC")
    List<AuxiliaryFile> findActivePrompts();

    /**
     * Buscar documentação
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType IN ('DOCUMENTATION', 'MARKDOWN') " +
           "AND af.isActive = true " +
           "ORDER BY af.name ASC")
    List<AuxiliaryFile> findDocumentation();

    /**
     * Buscar arquivos por nome parcial
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE LOWER(af.name) LIKE LOWER(CONCAT('%', :namePart, '%')) " +
           "ORDER BY af.name ASC")
    List<AuxiliaryFile> findByNameContaining(@Param("namePart") String namePart);

    /**
     * Buscar arquivos nunca utilizados
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.usageCount = 0 OR af.usageCount IS NULL " +
           "ORDER BY af.createdAt DESC")
    List<AuxiliaryFile> findUnusedFiles();

    /**
     * Buscar arquivos recentemente utilizados
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.lastUsedAt IS NOT NULL " +
           "ORDER BY af.lastUsedAt DESC")
    List<AuxiliaryFile> findRecentlyUsed();

    /**
     * Buscar arquivos por período de criação
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY af.createdAt DESC")
    List<AuxiliaryFile> findByCreationDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Buscar arquivos por tamanho
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileSize BETWEEN :minSize AND :maxSize " +
           "ORDER BY af.fileSize DESC")
    List<AuxiliaryFile> findBySizeRange(@Param("minSize") Long minSize,
                                      @Param("maxSize") Long maxSize);

    /**
     * Obter estatísticas de uso por tipo
     */
    @Query("SELECT af.fileType, COUNT(af), AVG(af.usageCount), SUM(af.fileSize) " +
           "FROM AuxiliaryFile af " +
           "GROUP BY af.fileType " +
           "ORDER BY COUNT(af) DESC")
    List<Object[]> getUsageStatisticsByType();

    /**
     * Buscar arquivos por versão
     */
    List<AuxiliaryFile> findByVersionOrderByNameAsc(String version);

    /**
     * Buscar arquivos por descrição
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE LOWER(af.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY af.name ASC")
    List<AuxiliaryFile> findByDescriptionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Verificar se nome existe
     */
    boolean existsByName(String name);

    /**
     * Obter total de arquivos por tipo
     */
    @Query("SELECT af.fileType, COUNT(af) " +
           "FROM AuxiliaryFile af " +
           "GROUP BY af.fileType " +
           "ORDER BY af.fileType")
    List<Object[]> countByFileType();

    /**
     * Buscar arquivos grandes (acima de um tamanho)
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileSize > :sizeThreshold " +
           "ORDER BY af.fileSize DESC")
    List<AuxiliaryFile> findLargeFiles(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Buscar arquivos modificados recentemente
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.updatedAt > af.createdAt " +
           "ORDER BY af.updatedAt DESC")
    List<AuxiliaryFile> findRecentlyModified();

    /**
     * Buscar templates de relatório
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType = 'TEMPLATE' " +
           "AND af.isActive = true " +
           "ORDER BY af.name ASC")
    List<AuxiliaryFile> findReportTemplates();

    /**
     * Buscar padrões de código
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType = 'CODE_PATTERN' " +
           "AND af.isActive = true " +
           "ORDER BY af.usageCount DESC")
    List<AuxiliaryFile> findCodePatterns();

    /**
     * Buscar configurações
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType = 'CONFIGURATION' " +
           "AND af.isActive = true " +
           "ORDER BY af.name ASC")
    List<AuxiliaryFile> findConfigurations();

    /**
     * Buscar rulesets ativos
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE af.fileType = 'RULESET' " +
           "AND af.isActive = true " +
           "ORDER BY af.version DESC, af.name ASC")
    List<AuxiliaryFile> findActiveRulesets();

    /**
     * Pesquisa global (nome, descrição, tags)
     */
    @Query("SELECT af FROM AuxiliaryFile af " +
           "WHERE (LOWER(af.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(af.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(af.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND af.isActive = true " +
           "ORDER BY af.usageCount DESC, af.name ASC")
    List<AuxiliaryFile> globalSearch(@Param("searchTerm") String searchTerm);

    /**
     * Obter arquivos por múltiplos IDs
     */
    @Query("SELECT af FROM AuxiliaryFile af WHERE af.id IN :ids ORDER BY af.name ASC")
    List<AuxiliaryFile> findByIds(@Param("ids") List<Long> ids);
}
