package com.sinqia.maya.repository;

import com.sinqia.maya.entity.ReviewExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewExportRepository extends JpaRepository<ReviewExport, String> {
    
    List<ReviewExport> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<ReviewExport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT re FROM ReviewExport re WHERE re.userId = :userId AND re.createdAt >= :since")
    List<ReviewExport> findRecentExportsByUser(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);
}
