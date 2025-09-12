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
 * Entidade que representa uma configura��o de prompt personalizada para revis�o de c�digo
 */
@Entity
@Table(name = "review_prompts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "prompt_template", columnDefinition = "TEXT", nullable = false)
    private String promptTemplate;

    @Column(name = "system_instructions", columnDefinition = "TEXT")
    private String systemInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptType type;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Column(name = "project_pattern")
    private String projectPattern;

    @Column(name = "file_extensions")
    private String fileExtensions = ".java,.js,.ts,.py,.cs";

    @Column(name = "focus_areas")
    private String focusAreas; // JSON array de �reas de foco

    @Column(name = "severity_levels")
    private String severityLevels = "CRITICAL,HIGH,MEDIUM,LOW";

    @Column(name = "max_tokens")
    private Integer maxTokens = 4000;

    @Column(name = "temperature")
    private Double temperature = 0.3;

    @OneToMany(mappedBy = "reviewPrompt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuxiliaryFile> auxiliaryFiles;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count")
    private Long usageCount = 0L;

    public enum PromptType {
        GENERAL,           // Prompt geral para qualquer c�digo
        JAVA_SPECIFIC,     // Espec�fico para Java
        JAVASCRIPT_SPECIFIC, // Espec�fico para JavaScript
        SECURITY_FOCUSED,  // Focado em seguran�a
        PERFORMANCE_FOCUSED, // Focado em performance
        SINQIA_STANDARDS,  // Padr�es espec�ficos Sinqia
        CONNECTION_LEAKS,  // Detec��o de vazamentos de conex�o
        CUSTOM            // Personalizado pelo usu�rio
    }

    // M�todos auxiliares
    public void incrementUsage() {
        this.usageCount = (this.usageCount != null ? this.usageCount : 0L) + 1L;
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isApplicableToFile(String fileName) {
        if (fileExtensions == null || fileExtensions.trim().isEmpty()) {
            return true;
        }
        
        String[] extensions = fileExtensions.split(",");
        for (String ext : extensions) {
            if (fileName.toLowerCase().endsWith(ext.trim().toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}
