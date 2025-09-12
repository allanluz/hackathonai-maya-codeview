package com.sinqia.maya.service;

import com.sinqia.maya.controller.ReviewExportController;
import com.sinqia.maya.entity.CodeReview;
import com.sinqia.maya.entity.ReviewExport;
import com.sinqia.maya.repository.CodeReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para exportação e gerenciamento de downloads de reviews
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReviewExportService {

    private final CodeReviewRepository codeReviewRepository;
    
    @Value("${maya.export.directory:${java.io.tmpdir}/maya-exports}")
    private String exportDirectory;

    @Value("${maya.export.retention-days:7}")
    private int retentionDays;

    /**
     * Exportar review em formato específico
     */
    public ReviewExport exportReview(Long reviewId, ReviewExportController.ExportReviewRequest request) {
        log.info("Iniciando exportação do review {} para formato {}", reviewId, request.format());

        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review não encontrado"));

        ReviewExport export = new ReviewExport();
        export.setCodeReview(review);
        export.setExportFormat(request.format());
        export.setStatus(ReviewExport.ExportStatus.PROCESSING);
        export.setCreatedBy("current-user"); // TODO: Obter usuário atual
        export.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));

        // Gerar nome do arquivo
        String fileName = generateFileName(review, request.format());
        export.setFileName(fileName);

        try {
            // Processar exportação baseada no formato
            String content = generateExportContent(review, request);
            Path filePath = saveExportFile(fileName, content);
            
            export.setFilePath(filePath.toString());
            export.setFileSize(Files.size(filePath));
            export.setStatus(ReviewExport.ExportStatus.COMPLETED);
            export.setExportedAt(LocalDateTime.now());

            log.info("Exportação concluída: {} -> {}", reviewId, fileName);

        } catch (Exception e) {
            log.error("Erro na exportação do review {}: {}", reviewId, e.getMessage());
            export.setStatus(ReviewExport.ExportStatus.FAILED);
            export.setErrorMessage(e.getMessage());
        }

        // TODO: Salvar no repositório ReviewExportRepository
        return export;
    }

    /**
     * Preparar download de arquivo exportado
     */
    public DownloadInfo prepareDownload(Long exportId) {
        log.info("Preparando download da exportação: {}", exportId);

        // TODO: Buscar do repositório
        ReviewExport export = new ReviewExport(); // Placeholder
        export.setId(exportId);
        export.setFileName("review-export.pdf");
        export.setFilePath("/tmp/test.pdf");

        if (export.isExpired()) {
            throw new IllegalArgumentException("Exportação expirada");
        }

        if (!ReviewExport.ExportStatus.COMPLETED.equals(export.getStatus())) {
            throw new IllegalArgumentException("Exportação não está pronta para download");
        }

        Path filePath = Paths.get(export.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Arquivo não encontrado");
        }

        export.incrementDownloadCount();
        // TODO: Salvar contador atualizado

        Resource resource = new FileSystemResource(filePath);
        String contentType = export.getExportFormat().getMimeType();

        return new DownloadInfo(resource, contentType, export.getFileName());
    }

    /**
     * Obter status de exportação
     */
    public ReviewExport getExportStatus(Long exportId) {
        // TODO: Buscar do repositório
        ReviewExport export = new ReviewExport();
        export.setId(exportId);
        export.setStatus(ReviewExport.ExportStatus.COMPLETED);
        export.setFileName("review-export.pdf");
        return export;
    }

    /**
     * Listar exportações de um review
     */
    public List<ReviewExport> getReviewExports(Long reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review não encontrado"));

        // TODO: Buscar exportações do repositório
        return new ArrayList<>();
    }

    /**
     * Listar exportações do usuário atual
     */
    public Page<ReviewExport> getUserExports(Pageable pageable, ReviewExport.ExportStatus status) {
        // TODO: Implementar busca por usuário e status
        return Page.empty();
    }

    /**
     * Deletar exportação
     */
    public void deleteExport(Long exportId) {
        // TODO: Buscar e deletar do repositório
        log.info("Exportação deletada: {}", exportId);
    }

    /**
     * Upload de review para repositório
     */
    public String uploadToRepository(Long reviewId, ReviewExportController.UploadToRepositoryRequest request) {
        log.info("Fazendo upload do review {} para repositório", reviewId);

        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review não encontrado"));

        // TODO: Implementar upload para GitHub/TFS
        if (request.createPullRequest()) {
            // Criar pull request com comentários da revisão
            return "https://github.com/example/repo/pull/123";
        } else {
            // Apenas fazer commit dos comentários
            return "Comentários adicionados ao commit " + review.getCommitSha();
        }
    }

    /**
     * Criar link de compartilhamento
     */
    public String createShareLink(Long reviewId, ReviewExportController.ShareReviewRequest request) {
        String shareId = UUID.randomUUID().toString();
        String shareLink = "https://maya.sinqia.com.br/shared/" + shareId;

        // TODO: Salvar informações de compartilhamento
        log.info("Link de compartilhamento criado para review {}: {}", reviewId, shareLink);

        return shareLink;
    }

    /**
     * Gerar preview de exportação
     */
    public String generatePreview(Long exportId) {
        // TODO: Gerar preview baseado no formato
        return "Preview do conteúdo exportado...";
    }

    /**
     * Exportação em lote
     */
    public List<Long> batchExport(ReviewExportController.BatchExportRequest request) {
        log.info("Iniciando exportação em lote de {} reviews", request.reviewIds().size());

        List<Long> exportIds = new ArrayList<>();

        for (Long reviewId : request.reviewIds()) {
            try {
                ReviewExportController.ExportReviewRequest exportRequest = 
                    new ReviewExportController.ExportReviewRequest(
                        request.format(),
                        true, true, true, true, // incluir tudo por padrão
                        request.options()
                    );

                ReviewExport export = exportReview(reviewId, exportRequest);
                exportIds.add(export.getId());

            } catch (Exception e) {
                log.error("Erro na exportação em lote do review {}: {}", reviewId, e.getMessage());
            }
        }

        log.info("Exportação em lote concluída: {} de {} reviews processados", 
                exportIds.size(), request.reviewIds().size());

        return exportIds;
    }

    // Métodos auxiliares privados

    private String generateFileName(CodeReview review, ReviewExport.ExportFormat format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseName = String.format("review_%s_%s_%s", 
                review.getRepositoryName(), 
                review.getCommitSha().substring(0, 8), 
                timestamp);
        return baseName + format.getExtension();
    }

    private String generateExportContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        switch (request.format()) {
            case PDF:
                return generatePdfContent(review, request);
            case MARKDOWN:
                return generateMarkdownContent(review, request);
            case HTML:
                return generateHtmlContent(review, request);
            case JSON:
                return generateJsonContent(review, request);
            case EXCEL:
                return generateExcelContent(review, request);
            case CSV:
                return generateCsvContent(review, request);
            default:
                throw new IllegalArgumentException("Formato não suportado: " + request.format());
        }
    }

    private Path saveExportFile(String fileName, String content) throws IOException {
        Path exportDir = Paths.get(exportDirectory);
        Files.createDirectories(exportDir);

        Path filePath = exportDir.resolve(fileName);
        Files.write(filePath, content.getBytes());

        return filePath;
    }

    private String generatePdfContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        // TODO: Implementar geração de PDF
        return "pdf-content-placeholder";
    }

    private String generateMarkdownContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        StringBuilder md = new StringBuilder();
        
        md.append("# Code Review Report\n\n");
        md.append("**Repository:** ").append(review.getRepositoryName()).append("\n");
        md.append("**Commit:** ").append(review.getCommitSha()).append("\n");
        md.append("**Author:** ").append(review.getAuthor()).append("\n");
        md.append("**Date:** ").append(review.getCreatedAt()).append("\n\n");
        
        md.append("## Summary\n\n");
        md.append("- **Quality Score:** ").append(review.getAnalysisScore()).append("/100\n");
        md.append("- **Critical Issues:** ").append(review.getCriticalIssues()).append("\n");
        md.append("- **Files Analyzed:** ").append(review.getTotalFiles()).append("\n\n");
        
        if (request.includeAnalysisDetails() && review.getFileAnalyses() != null) {
            md.append("## File Analysis\n\n");
            review.getFileAnalyses().forEach(fileAnalysis -> {
                md.append("### ").append(fileAnalysis.getFilePath()).append("\n\n");
                if (fileAnalysis.getIssues() != null) {
                    fileAnalysis.getIssues().forEach(issue -> {
                        md.append("- **").append(issue.getSeverity()).append(":** ");
                        md.append(issue.getDescription()).append("\n");
                    });
                }
                md.append("\n");
            });
        }
        
        return md.toString();
    }

    private String generateHtmlContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        // TODO: Implementar geração de HTML
        return "<html><body><h1>Code Review Report</h1></body></html>";
    }

    private String generateJsonContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        // TODO: Implementar serialização JSON completa
        return "{ \"review\": \"json-content\" }";
    }

    private String generateExcelContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        // TODO: Implementar geração de Excel
        return "excel-content-placeholder";
    }

    private String generateCsvContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        StringBuilder csv = new StringBuilder();
        csv.append("File,Issue Type,Severity,Description,Line\n");
        
        if (review.getFileAnalyses() != null) {
            review.getFileAnalyses().forEach(fileAnalysis -> {
                if (fileAnalysis.getIssues() != null) {
                    fileAnalysis.getIssues().forEach(issue -> {
                        csv.append(fileAnalysis.getFilePath()).append(",");
                        csv.append(issue.getType()).append(",");
                        csv.append(issue.getSeverity()).append(",");
                        csv.append("\"").append(issue.getDescription()).append("\",");
                        csv.append(issue.getLineNumber() != null ? issue.getLineNumber() : "").append("\n");
                    });
                }
            });
        }
        
        return csv.toString();
    }

    // Record para informações de download
    public record DownloadInfo(
            Resource resource,
            String contentType,
            String fileName
    ) {}
}
