package com.sinqia.maya.repository;

import com.sinqia.maya.entity.DashboardMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardMetricRepository extends JpaRepository<DashboardMetric, String> {
    
    Optional<DashboardMetric> findByMetricNameAndPeriod(String metricName, String period);
    
    List<DashboardMetric> findByPeriodOrderByCalculatedAtDesc(String period);
    
    List<DashboardMetric> findByCalculatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT dm FROM DashboardMetric dm WHERE dm.metricName = :metricName ORDER BY dm.calculatedAt DESC")
    List<DashboardMetric> findLatestByMetricName(@Param("metricName") String metricName);
    
    @Query("SELECT dm FROM DashboardMetric dm WHERE dm.period = :period AND dm.calculatedAt >= :since ORDER BY dm.calculatedAt DESC")
    List<DashboardMetric> findRecentMetrics(@Param("period") String period, @Param("since") LocalDateTime since);
    
    void deleteByCalculatedAtBefore(LocalDateTime cutoffDate);
}
