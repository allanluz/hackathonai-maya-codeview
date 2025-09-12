package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade para controle de exportações/downloads de reviews
 */
@Entity
@Table(name = "review_exports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_review_id", nullable = false)
    private CodeReview codeReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", nullable = false)
    private ExportFormat exportFormat;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExportStatus status = ExportStatus.PENDING;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "export_options", length = 1000)
    private String exportOptions; // JSON com opções da exportação

    public enum ExportFormat {
        PDF("application/pdf", ".pdf"),
        MARKDOWN("text/markdown", ".md"),
        HTML("text/html", ".html"),
        JSON("application/json", ".json"),
        EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
        CSV("text/csv", ".csv");

        private final String mimeType;
        private final String extension;

        ExportFormat(String mimeType, String extension) {
            this.mimeType = mimeType;
            this.extension = extension;
        }

        public String getMimeType() { return mimeType; }
        public String getExtension() { return extension; }
    }

    public enum ExportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        EXPIRED
    }

    // Métodos auxiliares
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount != null ? this.downloadCount : 0) + 1;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 bytes";
        }
        
        long size = fileSize;
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
