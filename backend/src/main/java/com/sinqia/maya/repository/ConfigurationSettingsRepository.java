package com.sinqia.maya.repository;

import com.sinqia.maya.entity.ConfigurationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ConfigurationSettings operations.
 * 
 * Provides methods for managing MAYA system configurations,
 * including search by key, category and data type.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public interface ConfigurationSettingsRepository extends JpaRepository<ConfigurationSettings, Long> {

    /**
     * Find configuration by key name
     */
    Optional<ConfigurationSettings> findByKeyName(String keyName);

    /**
     * Find configurations by category and active status
     */
    List<ConfigurationSettings> findByCategoryAndIsActiveTrue(String category);

    /**
     * Find all active configurations ordered by category
     */
    List<ConfigurationSettings> findByIsActiveTrueOrderByCategory();

    /**
     * Find configurations by category
     */
    List<ConfigurationSettings> findByCategoryOrderByKeyNameAsc(String category);

    /**
     * Find configurations by data type
     */
    List<ConfigurationSettings> findByDataType(ConfigurationSettings.DataType dataType);

    /**
     * Find sensitive configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs WHERE cs.isSensitive = true")
    List<ConfigurationSettings> findSensitiveSettings();

    /**
     * Find required configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs WHERE cs.isRequired = true")
    List<ConfigurationSettings> findRequiredSettings();

    /**
     * Find configurations by key prefix
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.keyName LIKE :prefix% " +
           "ORDER BY cs.keyName ASC")
    List<ConfigurationSettings> findByKeyPrefix(@Param("prefix") String prefix);

    /**
     * Check if key exists
     */
    boolean existsByKeyName(String keyName);

    /**
     * Get value by key (returns only the value)
     */
    @Query("SELECT cs.value FROM ConfigurationSettings cs WHERE cs.keyName = :keyName")
    Optional<String> findValueByKeyName(@Param("keyName") String keyName);

    /**
     * Find recently modified configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.updatedAt > cs.createdAt " +
           "ORDER BY cs.updatedAt DESC")
    List<ConfigurationSettings> findRecentlyModified();

    /**
     * Get all available categories
     */
    @Query("SELECT DISTINCT cs.category FROM ConfigurationSettings cs " +
           "WHERE cs.category IS NOT NULL " +
           "ORDER BY cs.category")
    List<String> findAllCategories();

    /**
     * Find configurations by multiple keys
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.keyName IN :keyNames " +
           "ORDER BY cs.keyName ASC")
    List<ConfigurationSettings> findByKeyNames(@Param("keyNames") List<String> keyNames);

    /**
     * Count configurations by category
     */
    @Query("SELECT cs.category, COUNT(cs) " +
           "FROM ConfigurationSettings cs " +
           "WHERE cs.category IS NOT NULL " +
           "GROUP BY cs.category " +
           "ORDER BY cs.category")
    List<Object[]> countByCategory();

    /**
     * Find configurations with default value
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.defaultValue IS NOT NULL " +
           "AND LENGTH(cs.defaultValue) > 0")
    List<ConfigurationSettings> findWithDefaultValue();

    /**
     * Find empty or null configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.value IS NULL OR LENGTH(TRIM(cs.value)) = 0")
    List<ConfigurationSettings> findEmptyConfigurations();

    /**
     * Find system configurations (prefix 'maya.')
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.keyName LIKE 'maya.%' " +
           "ORDER BY cs.keyName ASC")
    List<ConfigurationSettings> findSystemConfigurations();

    /**
     * Find integration configurations (prefix 'tfs.' or 'ai.')
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.keyName LIKE 'tfs.%' OR cs.keyName LIKE 'ai.%' " +
           "ORDER BY cs.keyName ASC")
    List<ConfigurationSettings> findIntegrationConfigurations();

    /**
     * Validate required configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.isRequired = true " +
           "AND (cs.value IS NULL OR LENGTH(TRIM(cs.value)) = 0)")
    List<ConfigurationSettings> findMissingRequiredConfigurations();

    /**
     * Find configurations by description (text search)
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE LOWER(cs.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(cs.keyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ConfigurationSettings> searchByDescription(@Param("searchTerm") String searchTerm);

    /**
     * Get MAYA analysis configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.category = 'ANALYSIS' " +
           "OR cs.keyName LIKE 'maya.analysis.%' " +
           "ORDER BY cs.keyName ASC")
    List<ConfigurationSettings> findAnalysisConfigurations();

    /**
     * Get threshold/limit configurations
     */
    @Query("SELECT cs FROM ConfigurationSettings cs " +
           "WHERE cs.keyName LIKE '%.threshold.%' " +
           "OR cs.keyName LIKE '%.limit.%' " +
           "ORDER BY cs.keyName ASC")
    List<ConfigurationSettings> findThresholdConfigurations();
}
