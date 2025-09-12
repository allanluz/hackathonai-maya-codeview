package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade que representa um repositório conectado ao sistema MAYA.
 * Suporta tanto GitHub quanto TFS/Azure DevOps.
 */
@Entity
@Table(name = "repositories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepositoryType type;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "default_branch")
    private String defaultBranch = "main";

    @Column(name = "auto_review_enabled")
    private Boolean autoReviewEnabled = false;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CodeReview> codeReviews;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    public enum RepositoryType {
        GITHUB,
        TFS,
        AZURE_DEVOPS
    }

    // Métodos auxiliares
    public boolean isGitHub() {
        return RepositoryType.GITHUB.equals(this.type);
    }

    public boolean isTfs() {
        return RepositoryType.TFS.equals(this.type) || RepositoryType.AZURE_DEVOPS.equals(this.type);
    }
}
