package com.sinqia.maya.repository;

import com.sinqia.maya.entity.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository as RepositoryAnnotation;

import java.util.List;
import java.util.Optional;

@RepositoryAnnotation
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    Optional<Repository> findByUrl(String url);

    List<Repository> findByTypeAndIsActiveTrue(Repository.RepositoryType type);

    List<Repository> findByIsActiveTrue();

    Page<Repository> findByIsActiveTrueOrderByNameAsc(Pageable pageable);

    @Query("SELECT r FROM Repository r WHERE r.isActive = true AND r.autoReviewEnabled = true")
    List<Repository> findActiveRepositoriesWithAutoReview();

    @Query("SELECT r FROM Repository r WHERE r.organizationName = :org AND r.isActive = true")
    List<Repository> findByOrganizationAndActive(@Param("org") String organizationName);

    @Query("SELECT COUNT(cr) FROM CodeReview cr WHERE cr.repository.id = :repoId")
    long countReviewsByRepository(@Param("repoId") Long repositoryId);

    boolean existsByUrl(String url);
}
