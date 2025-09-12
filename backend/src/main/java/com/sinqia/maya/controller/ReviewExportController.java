package com.sinqia.maya.controller;

import com.sinqia.maya.entity.ReviewExport;
import com.sinqia.maya.service.ReviewExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller para upload e download de reviews
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ReviewExportController {

    private final ReviewExportService reviewExportService;

    /**
     * Exportar review em formato específico
     */
    @PostMapping("/{reviewId}/export")
    public ResponseEntity<ExportReviewResponse> exportReview(
            @PathVariable Long reviewId,
            @RequestBody ExportReviewRequest request) {
        
        log.info("Exportando review {} para formato: {}", reviewId, request.format());
        
        try {
            ReviewExport export = reviewExportService.exportReview(reviewId, request);
            
            return ResponseEntity.ok(new ExportReviewResponse(
                    true,
                    "Exportação iniciada com sucesso",
                    export.getId(),
                    export.getStatus(),
                    export.getFileName()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao exportar review {}: {}", reviewId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new ExportReviewResponse(false, e.getMessage(), null, null, null));
        }
    }

    /**
     * Download de arquivo exportado
     */
    @GetMapping("/exports/{exportId}/download")
    public ResponseEntity<Resource> downloadExport(@PathVariable Long exportId) {
        log.info("Download da exportação: {}", exportId);
        
        try {
            ReviewExportService.DownloadInfo downloadInfo = reviewExportService.prepareDownload(exportId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + downloadInfo.fileName() + "\"")
                    .body(downloadInfo.resource());
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro no download da exportação {}: {}", exportId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obter status de uma exportação
     */
    @GetMapping("/exports/{exportId}/status")
    public ResponseEntity<ExportStatusResponse> getExportStatus(@PathVariable Long exportId) {
        log.debug("Verificando status da exportação: {}", exportId);
        
        try {
            ReviewExport export = reviewExportService.getExportStatus(exportId);
            
            return ResponseEntity.ok(new ExportStatusResponse(
                    export.getId(),
                    export.getStatus(),
                    export.getFileName(),
                    export.getFileSize(),
                    export.getFormattedFileSize(),
                    export.getDownloadCount(),
                    export.getCreatedAt(),
                    export.getExportedAt(),
                    export.getExpiresAt(),
                    export.getErrorMessage()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar exportações de um review
     */
    @GetMapping("/{reviewId}/exports")
    public ResponseEntity<List<ReviewExport>> getReviewExports(@PathVariable Long reviewId) {
        log.debug("Listando exportações do review: {}", reviewId);
        
        try {
            List<ReviewExport> exports = reviewExportService.getReviewExports(reviewId);
            return ResponseEntity.ok(exports);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todas as exportações do usuário
     */
    @GetMapping("/exports")
    public ResponseEntity<Page<ReviewExport>> getUserExports(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) ReviewExport.ExportStatus status) {
        
        log.debug("Listando exportações do usuário - status: {}", status);
        
        Page<ReviewExport> exports = reviewExportService.getUserExports(pageable, status);
        return ResponseEntity.ok(exports);
    }

    /**
     * Deletar exportação
     */
    @DeleteMapping("/exports/{exportId}")
    public ResponseEntity<DeleteExportResponse> deleteExport(@PathVariable Long exportId) {
        log.info("Deletando exportação: {}", exportId);
        
        try {
            reviewExportService.deleteExport(exportId);
            
            return ResponseEntity.ok(new DeleteExportResponse(
                    true,
                    "Exportação deletada com sucesso"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao deletar exportação {}: {}", exportId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new DeleteExportResponse(false, e.getMessage()));
        }
    }

    /**
     * Upload de review comentado de volta para o repositório
     */
    @PostMapping("/{reviewId}/upload-to-repository")
    public ResponseEntity<UploadToRepositoryResponse> uploadToRepository(
            @PathVariable Long reviewId,
            @RequestBody UploadToRepositoryRequest request) {
        
        log.info("Fazendo upload de review {} para repositório", reviewId);
        
        try {
            String result = reviewExportService.uploadToRepository(reviewId, request);
            
            return ResponseEntity.ok(new UploadToRepositoryResponse(
                    true,
                    "Review enviado para o repositório com sucesso",
                    result
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao fazer upload do review {} para repositório: {}", reviewId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new UploadToRepositoryResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Compartilhar review via link
     */
    @PostMapping("/{reviewId}/share")
    public ResponseEntity<ShareReviewResponse> shareReview(
            @PathVariable Long reviewId,
            @RequestBody ShareReviewRequest request) {
        
        log.info("Compartilhando review: {}", reviewId);
        
        try {
            String shareLink = reviewExportService.createShareLink(reviewId, request);
            
            return ResponseEntity.ok(new ShareReviewResponse(
                    true,
                    "Link de compartilhamento criado",
                    shareLink,
                    request.expiresIn()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao compartilhar review {}: {}", reviewId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new ShareReviewResponse(false, e.getMessage(), null, null));
        }
    }

    /**
     * Preview de exportação (primeiras linhas/páginas)
     */
    @GetMapping("/exports/{exportId}/preview")
    public ResponseEntity<ExportPreviewResponse> previewExport(@PathVariable Long exportId) {
        log.debug("Gerando preview da exportação: {}", exportId);
        
        try {
            String preview = reviewExportService.generatePreview(exportId);
            
            return ResponseEntity.ok(new ExportPreviewResponse(
                    true,
                    "Preview gerado com sucesso",
                    preview
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao gerar preview da exportação {}: {}", exportId, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new ExportPreviewResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Exportação em lote de múltiplos reviews
     */
    @PostMapping("/batch-export")
    public ResponseEntity<BatchExportResponse> batchExport(@RequestBody BatchExportRequest request) {
        log.info("Exportação em lote de {} reviews", request.reviewIds().size());
        
        try {
            List<Long> exportIds = reviewExportService.batchExport(request);
            
            return ResponseEntity.ok(new BatchExportResponse(
                    true,
                    "Exportação em lote iniciada",
                    exportIds.size(),
                    exportIds
            ));
            
        } catch (Exception e) {
            log.error("Erro na exportação em lote: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new BatchExportResponse(false, e.getMessage(), 0, null));
        }
    }

    // DTOs

    public record ExportReviewRequest(
            ReviewExport.ExportFormat format,
            boolean includeSourceCode,
            boolean includeAnalysisDetails,
            boolean includeCharts,
            boolean includeRecommendations,
            Map<String, Object> customOptions
    ) {}

    public record ExportReviewResponse(
            boolean success,
            String message,
            Long exportId,
            ReviewExport.ExportStatus status,
            String fileName
    ) {}

    public record ExportStatusResponse(
            Long exportId,
            ReviewExport.ExportStatus status,
            String fileName,
            Long fileSize,
            String formattedFileSize,
            Integer downloadCount,
            LocalDateTime createdAt,
            LocalDateTime exportedAt,
            LocalDateTime expiresAt,
            String errorMessage
    ) {}

    public record DeleteExportResponse(
            boolean success,
            String message
    ) {}

    public record UploadToRepositoryRequest(
            String targetBranch,
            String commitMessage,
            boolean createPullRequest,
            List<String> reviewers,
            Map<String, Object> options
    ) {}

    public record UploadToRepositoryResponse(
            boolean success,
            String message,
            String pullRequestUrl
    ) {}

    public record ShareReviewRequest(
            int expiresIn, // dias
            boolean requiresPassword,
            String password,
            List<String> allowedEmails
    ) {}

    public record ShareReviewResponse(
            boolean success,
            String message,
            String shareLink,
            Integer expiresIn
    ) {}

    public record ExportPreviewResponse(
            boolean success,
            String message,
            String preview
    ) {}

    public record BatchExportRequest(
            List<Long> reviewIds,
            ReviewExport.ExportFormat format,
            String zipFileName,
            Map<String, Object> options
    ) {}

    public record BatchExportResponse(
            boolean success,
            String message,
            int processedCount,
            List<Long> exportIds
    ) {}
}
