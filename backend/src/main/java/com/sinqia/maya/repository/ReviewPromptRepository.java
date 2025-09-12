package com.sinqia.maya.repository;

import com.sinqia.maya.entity.ReviewPrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewPromptRepository extends JpaRepository<ReviewPrompt, Long> {

    List<ReviewPrompt> findByIsActiveTrueOrderByNameAsc();

    Optional<ReviewPrompt> findByIsDefaultTrueAndIsActiveTrue();

    List<ReviewPrompt> findByTypeAndIsActiveTrueOrderByNameAsc(ReviewPrompt.PromptType type);

    Page<ReviewPrompt> findByIsActiveTrueOrderByNameAsc(Pageable pageable);

    @Query("SELECT rp FROM ReviewPrompt rp WHERE rp.isActive = true AND " +
           "(rp.repository IS NULL OR rp.repository.id = :repositoryId) " +
           "ORDER BY rp.repository.id DESC, rp.isDefault DESC, rp.name ASC")
    List<ReviewPrompt> findApplicablePrompts(@Param("repositoryId") Long repositoryId);

    @Query("SELECT rp FROM ReviewPrompt rp WHERE rp.isActive = true AND " +
           "rp.projectPattern IS NULL OR :projectName LIKE CONCAT('%', rp.projectPattern, '%') " +
           "ORDER BY rp.usageCount DESC")
    List<ReviewPrompt> findByProjectPattern(@Param("projectName") String projectName);

    @Query("SELECT rp FROM ReviewPrompt rp WHERE rp.createdBy = :userId AND rp.isActive = true " +
           "ORDER BY rp.lastUsedAt DESC NULLS LAST")
    List<ReviewPrompt> findByUser(@Param("userId") String userId);

    @Query("SELECT rp FROM ReviewPrompt rp WHERE rp.isActive = true " +
           "ORDER BY rp.usageCount DESC, rp.lastUsedAt DESC NULLS LAST")
    Page<ReviewPrompt> findMostUsed(Pageable pageable);

    boolean existsByNameAndIsActiveTrue(String name);
}
