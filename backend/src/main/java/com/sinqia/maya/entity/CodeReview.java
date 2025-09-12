package com.sinqia.maya.entity;

import jakarta.    /**
     * Repository name
     */
    @Column(name = "repository_name", nullable = false, length = 200)
    private String repositoryName;

    /**
     * Repository entity reference
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;stence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entity representing a code review in MAYA system.
 * 
 * This entity stores information about analyzed commits, including
 * quality metrics, found issues and MAYA analysis results.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Entity
@Table(name = "code_reviews", indexes = {
    @Index(name = "idx_commit_sha", columnList = "commit_sha"),
    @Index(name = "idx_repository_name", columnList = "repository_name"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"fileAnalyses"})
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pull Request ID (optional)
     */
    @Column(name = "pull_request_id")
    private String pullRequestId;

    /**
     * Commit SHA being analyzed
     */
    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;

    /**
     * Repository name
     */
    @Column(name = "repository_name", nullable = false, length = 255)
    private String repositoryName;

    /**
     * Project name
     */
    @Column(name = "project_name", length = 255)
    private String projectName;

    /**
     * Branch name
     */
    @Column(name = "branch_name", length = 100)
    private String branchName;

    /**
     * Author of the commit
     */
    @Column(name = "author", nullable = false, length = 255)
    private String author;

    /**
     * Author email
     */
    @Column(name = "author_email", length = 255)
    private String authorEmail;

    /**
     * Commit message
     */
    @Column(name = "commit_message", columnDefinition = "TEXT")
    private String commitMessage;

    /**
     * Analysis status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;

    /**
     * Analysis score (0-100)
     */
    @Column(name = "analysis_score")
    private Double analysisScore;

    /**
     * Critical issues count
     */
    @Column(name = "critical_issues")
    private Integer criticalIssues = 0;

    /**
     * High priority issues count
     */
    @Column(name = "high_issues")
    private Integer highIssues = 0;

    /**
     * Medium priority issues count
     */
    @Column(name = "medium_issues")
    private Integer mediumIssues = 0;

    /**
     * Low priority issues count
     */
    @Column(name = "low_issues")
    private Integer lowIssues = 0;

    /**
     * Total files analyzed
     */
    @Column(name = "total_files")
    private Integer totalFiles = 0;

    /**
     * Total lines of code analyzed
     */
    @Column(name = "total_lines")
    private Integer totalLines = 0;

    /**
     * Analysis duration in milliseconds
     */
    @Column(name = "analysis_duration_ms")
    private Long analysisDurationMs;

    /**
     * LLM model used for analysis
     */
    @Column(name = "llm_model", length = 50)
    private String llmModel;

    /**
     * Review prompt used for analysis
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_prompt_id")
    private ReviewPrompt reviewPrompt;

    /**
     * AI confidence score (0-1)
     */
    @Column(name = "ai_confidence")
    private Double aiConfidence;

    /**
     * Analysis summary
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * Analysis recommendations
     */
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    /**
     * Error message if analysis failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Review title
     */
    @Column(name = "title", length = 500)
    private String title;

    /**
     * Creation timestamp
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * File analyses associated with this review
     */
    @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FileAnalysis> fileAnalyses = new ArrayList<>();

    /**
     * Review status enumeration
     */
    public enum ReviewStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled");

        private final String description;

        ReviewStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Default constructor
     */
    public CodeReview() {}

    /**
     * Constructor with basic fields
     */
    public CodeReview(String commitSha, String repositoryName, String author) {
        this.commitSha = commitSha;
        this.repositoryName = repositoryName;
        this.author = author;
    }

    /**
     * Constructor with complete fields
     */
    public CodeReview(String commitSha, String repositoryName, String projectName, 
                     String branchName, String author, String authorEmail, String commitMessage) {
        this.commitSha = commitSha;
        this.repositoryName = repositoryName;
        this.projectName = projectName;
        this.branchName = branchName;
        this.author = author;
        this.authorEmail = authorEmail;
        this.commitMessage = commitMessage;
    }

    /**
     * Check if analysis is completed
     */
    public boolean isCompleted() {
        return status == ReviewStatus.COMPLETED;
    }

    /**
     * Check if analysis failed
     */
    public boolean isFailed() {
        return status == ReviewStatus.FAILED;
    }

    /**
     * Check if analysis is in progress
     */
    public boolean isInProgress() {
        return status == ReviewStatus.IN_PROGRESS;
    }

    /**
     * Get total issues count
     */
    public int getTotalIssues() {
        return (criticalIssues != null ? criticalIssues : 0) +
               (highIssues != null ? highIssues : 0) +
               (mediumIssues != null ? mediumIssues : 0) +
               (lowIssues != null ? lowIssues : 0);
    }

    /**
     * Check if has critical issues
     */
    public boolean hasCriticalIssues() {
        return criticalIssues != null && criticalIssues > 0;
    }

    /**
     * Get analysis duration in seconds
     */
    public double getAnalysisDurationSeconds() {
        return analysisDurationMs != null ? analysisDurationMs / 1000.0 : 0.0;
    }

    /**
     * Calculate quality grade based on score
     */
    public String getQualityGrade() {
        if (analysisScore == null) return "N/A";
        if (analysisScore >= 90) return "A";
        if (analysisScore >= 80) return "B";
        if (analysisScore >= 70) return "C";
        if (analysisScore >= 60) return "D";
        return "F";
    }

    /**
     * Check if review needs attention (has critical issues or low score)
     */
    public boolean needsAttention() {
        return hasCriticalIssues() || (analysisScore != null && analysisScore < 70);
    }

    /**
     * Add file analysis to this review
     */
    public void addFileAnalysis(FileAnalysis fileAnalysis) {
        if (fileAnalyses == null) {
            fileAnalyses = new ArrayList<>();
        }
        fileAnalyses.add(fileAnalysis);
        fileAnalysis.setCodeReview(this);
    }

    /**
     * Calculate metrics based on file analyses
     */
    public void calculateMetrics() {
        if (fileAnalyses == null || fileAnalyses.isEmpty()) {
            return;
        }

        // Reset counters
        criticalIssues = 0;
        highIssues = 0;
        mediumIssues = 0;
        lowIssues = 0;
        totalFiles = fileAnalyses.size();
        totalLines = 0;

        // Calculate metrics from file analyses
        for (FileAnalysis analysis : fileAnalyses) {
            if (analysis.getLinesOfCode() != null) {
                totalLines += analysis.getLinesOfCode();
            }
            
            // Note: Issue counting implementation depends on AnalysisIssue entity
            // This is a placeholder implementation
        }

        // Calculate overall score
        int totalIssues = getTotalIssues();
        if (totalLines > 0) {
            double issueRate = (double) totalIssues / totalLines * 100;
            analysisScore = Math.max(0, 100 - issueRate * 10);
        }
    }

    /**
     * Set review comment
     */
    public void setReviewComment(String comment) {
        this.summary = comment;
    }

    /**
     * Get title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
