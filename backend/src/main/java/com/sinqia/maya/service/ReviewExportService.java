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
 * Servi�o para exporta��o e gerenciamento de downloads de reviews
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
     * Exportar review em formato espec�fico
     */
    public ReviewExport exportReview(Long reviewId, ReviewExportController.ExportReviewRequest request) {
        log.info("Iniciando exporta��o do review {} para formato {}", reviewId, request.format());

        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review n�o encontrado"));

        ReviewExport export = new ReviewExport();
        export.setCodeReview(review);
        export.setExportFormat(request.format());
        export.setStatus(ReviewExport.ExportStatus.PROCESSING);
        export.setCreatedBy("current-user"); // TODO: Obter usu�rio atual
        export.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));

        // Gerar nome do arquivo
        String fileName = generateFileName(review, request.format());
        export.setFileName(fileName);

        try {
            // Processar exporta��o baseada no formato
            String content = generateExportContent(review, request);
            Path filePath = saveExportFile(fileName, content);
            
            export.setFilePath(filePath.toString());
            export.setFileSize(Files.size(filePath));
            export.setStatus(ReviewExport.ExportStatus.COMPLETED);
            export.setExportedAt(LocalDateTime.now());

            log.info("Exporta��o conclu�da: {} -> {}", reviewId, fileName);

        } catch (Exception e) {
            log.error("Erro na exporta��o do review {}: {}", reviewId, e.getMessage());
            export.setStatus(ReviewExport.ExportStatus.FAILED);
            export.setErrorMessage(e.getMessage());
        }

        // TODO: Salvar no reposit�rio ReviewExportRepository
        return export;
    }

    /**
     * Preparar download de arquivo exportado
     */
    public DownloadInfo prepareDownload(Long exportId) {
        log.info("Preparando download da exporta��o: {}", exportId);

        // TODO: Buscar do reposit�rio
        ReviewExport export = new ReviewExport(); // Placeholder
        export.setId(exportId);
        export.setFileName("review-export.pdf");
        export.setFilePath("/tmp/test.pdf");

        if (export.isExpired()) {
            throw new IllegalArgumentException("Exporta��o expirada");
        }

        if (!ReviewExport.ExportStatus.COMPLETED.equals(export.getStatus())) {
            throw new IllegalArgumentException("Exporta��o n�o est� pronta para download");
        }

        Path filePath = Paths.get(export.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Arquivo n�o encontrado");
        }

        export.incrementDownloadCount();
        // TODO: Salvar contador atualizado

        Resource resource = new FileSystemResource(filePath);
        String contentType = export.getExportFormat().getMimeType();

        return new DownloadInfo(resource, contentType, export.getFileName());
    }

    /**
     * Obter status de exporta��o
     */
    public ReviewExport getExportStatus(Long exportId) {
        // TODO: Buscar do reposit�rio
        ReviewExport export = new ReviewExport();
        export.setId(exportId);
        export.setStatus(ReviewExport.ExportStatus.COMPLETED);
        export.setFileName("review-export.pdf");
        return export;
    }

    /**
     * Listar exporta��es de um review
     */
    public List<ReviewExport> getReviewExports(Long reviewId) {
        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review n�o encontrado"));

        // TODO: Buscar exporta��es do reposit�rio
        return new ArrayList<>();
    }

    /**
     * Listar exporta��es do usu�rio atual
     */
    public Page<ReviewExport> getUserExports(Pageable pageable, ReviewExport.ExportStatus status) {
        // TODO: Implementar busca por usu�rio e status
        return Page.empty();
    }

    /**
     * Deletar exporta��o
     */
    public void deleteExport(Long exportId) {
        // TODO: Buscar e deletar do reposit�rio
        log.info("Exporta��o deletada: {}", exportId);
    }

    /**
     * Upload de review para reposit�rio
     */
    public String uploadToRepository(Long reviewId, ReviewExportController.UploadToRepositoryRequest request) {
        log.info("Fazendo upload do review {} para reposit�rio", reviewId);

        CodeReview review = codeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review n�o encontrado"));

        // TODO: Implementar upload para GitHub/TFS
        if (request.createPullRequest()) {
            // Criar pull request com coment�rios da revis�o
            return "https://github.com/example/repo/pull/123";
        } else {
            // Apenas fazer commit dos coment�rios
            return "Coment�rios adicionados ao commit " + review.getCommitSha();
        }
    }

    /**
     * Criar link de compartilhamento
     */
    public String createShareLink(Long reviewId, ReviewExportController.ShareReviewRequest request) {
        String shareId = UUID.randomUUID().toString();
        String shareLink = "https://maya.sinqia.com.br/shared/" + shareId;

        // TODO: Salvar informa��es de compartilhamento
        log.info("Link de compartilhamento criado para review {}: {}", reviewId, shareLink);

        return shareLink;
    }

    /**
     * Gerar preview de exporta��o
     */
    public String generatePreview(Long exportId) {
        // TODO: Gerar preview baseado no formato
        return "Preview do conte�do exportado...";
    }

    /**
     * Exporta��o em lote
     */
    public List<Long> batchExport(ReviewExportController.BatchExportRequest request) {
        log.info("Iniciando exporta��o em lote de {} reviews", request.reviewIds().size());

        List<Long> exportIds = new ArrayList<>();

        for (Long reviewId : request.reviewIds()) {
            try {
                ReviewExportController.ExportReviewRequest exportRequest = 
                    new ReviewExportController.ExportReviewRequest(
                        request.format(),
                        true, true, true, true, // incluir tudo por padr�o
                        request.options()
                    );

                ReviewExport export = exportReview(reviewId, exportRequest);
                exportIds.add(export.getId());

            } catch (Exception e) {
                log.error("Erro na exporta��o em lote do review {}: {}", reviewId, e.getMessage());
            }
        }

        log.info("Exporta��o em lote conclu�da: {} de {} reviews processados", 
                exportIds.size(), request.reviewIds().size());

        return exportIds;
    }

    // M�todos auxiliares privados

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
                throw new IllegalArgumentException("Formato n�o suportado: " + request.format());
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
        // TODO: Implementar gera��o de PDF
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
        // TODO: Implementar gera��o de HTML
        return "<html><body><h1>Code Review Report</h1></body></html>";
    }

    private String generateJsonContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        // TODO: Implementar serializa��o JSON completa
        return "{ \"review\": \"json-content\" }";
    }

    private String generateExcelContent(CodeReview review, ReviewExportController.ExportReviewRequest request) {
        // TODO: Implementar gera��o de Excel
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

    // Record para informa��es de download
    public record DownloadInfo(
            Resource resource,
            String contentType,
            String fileName
    ) {}
}
