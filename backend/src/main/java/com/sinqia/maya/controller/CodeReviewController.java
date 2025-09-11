package com.sinqia.maya.controller;

import com.sinqia.maya.entity.CodeReview;
import com.sinqia.maya.entity.FileAnalysis;
import com.sinqia.maya.service.MayaAnalysisService;
import com.sinqia.maya.service.SinqiaAiService;
import com.sinqia.maya.repository.CodeReviewRepository;
import com.sinqia.maya.repository.impl.CodeReviewRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller REST para an�lises de c�digo MAYA.
 * 
 * Endpoints para:
 * - Gerenciar revis�es de c�digo
 * - Executar an�lises
 * - Obter relat�rios
 * - Dashboard e m�tricas
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/code-reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class CodeReviewController {

    private final MayaAnalysisService mayaAnalysisService;
    private final SinqiaAiService sinqiaAiService;
    private final CodeReviewRepository codeReviewRepository;
    private final CodeReviewRepositoryImpl codeReviewRepositoryImpl;

    /**
     * Listar todas as revis�es de c�digo com pagina��o
     */
    @GetMapping
    public ResponseEntity<Page<CodeReview>> getAllCodeReviews(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String repositoryName,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) CodeReview.ReviewStatus status) {
        
        log.debug("Listando revis�es de c�digo - repo: {}, projeto: {}, autor: {}, status: {}", 
                repositoryName, projectName, author, status);
        
        Page<CodeReview> reviews;
        
        if (repositoryName != null || projectName != null || author != null || status != null) {
            // Filtros aplicados - usar repository customizado
            reviews = codeReviewRepositoryImpl.findWithFilters(repositoryName, projectName, author, status, pageable);
        } else {
            // Sem filtros
            reviews = codeReviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * Obter detalhes de uma revis�o espec�fica
     */
    @GetMapping("/{id}")
    public ResponseEntity<CodeReview> getCodeReviewById(@PathVariable Long id) {
        log.debug("Buscando revis�o de c�digo: {}", id);
        
        Optional<CodeReview> review = codeReviewRepository.findById(id);
        return review.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obter revis�o por commit SHA
     */
    @GetMapping("/commit/{commitSha}")
    public ResponseEntity<CodeReview> getCodeReviewByCommit(@PathVariable String commitSha) {
        log.debug("Buscando revis�o por commit: {}", commitSha);
        
        Optional<CodeReview> review = codeReviewRepository.findByCommitSha(commitSha);
        return review.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Executar an�lise manual de commit
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeCommit(@RequestBody AnalysisRequest request) {
        log.info("Iniciando an�lise manual para commit: {}", request.commitSha());
        
        try {
            CodeReview review = mayaAnalysisService.analyzeCommit(
                    request.commitSha(),
                    request.repositoryName(),
                    request.projectName(),
                    request.author(),
                    request.title(),
                    request.javaFiles(),
                    request.llmModel()
            );
            
            if (review != null) {
                return ResponseEntity.ok(new AnalysisResponse(
                        true,
                        "An�lise conclu�da com sucesso",
                        review.getId(),
                        review.getStatus(),
                        review.getAnalysisScore(),
                        review.getCriticalIssues(),
                        review.getAnalysisDurationMs()
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(new AnalysisResponse(false, "Falha na an�lise", null, null, null, null, null));
            }
            
        } catch (Exception e) {
            log.error("Erro durante an�lise manual do commit {}: {}", request.commitSha(), e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new AnalysisResponse(false, "Erro interno: " + e.getMessage(), null, null, null, null, null));
        }
    }

    /**
     * Obter an�lises de arquivos de uma revis�o
     */
    @GetMapping("/{id}/files")
    public ResponseEntity<List<FileAnalysis>> getFileAnalyses(@PathVariable Long id) {
        log.debug("Buscando an�lises de arquivos para revis�o: {}", id);
        
        Optional<CodeReview> review = codeReviewRepository.findById(id);
        if (review.isPresent()) {
            return ResponseEntity.ok(review.get().getFileAnalyses());
        }
        
        return ResponseEntity.notFound().build();
    }

    /**
     * Obter estat�sticas do dashboard
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStats> getDashboardStats(
            @RequestParam(required = false, defaultValue = "30") int days) {
        
        log.debug("Gerando estat�sticas do dashboard para {} dias", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        
        DashboardStats stats = new DashboardStats(
                codeReviewRepository.countByCreatedAtAfter(cutoffDate),
                codeReviewRepository.countByStatusAndCreatedAtAfter(CodeReview.ReviewStatus.COMPLETED, cutoffDate),
                codeReviewRepository.countByStatusAndCreatedAtAfter(CodeReview.ReviewStatus.FAILED, cutoffDate),
                codeReviewRepository.findAverageScoreAfter(cutoffDate),
                codeReviewRepository.countCriticalIssuesAfter(cutoffDate),
                codeReviewRepository.findTopRepositoriesByReviewCount(cutoffDate),
                codeReviewRepository.findTopAuthorsByReviewCount(cutoffDate)
        );
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Obter m�tricas de qualidade por reposit�rio
     */
    @GetMapping("/metrics/repository")
    public ResponseEntity<List<RepositoryMetrics>> getRepositoryMetrics(
            @RequestParam(required = false, defaultValue = "30") int days) {
        
        log.debug("Gerando m�tricas por reposit�rio para {} dias", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<Object[]> rawMetrics = codeReviewRepository.findRepositoryMetrics(cutoffDate);
        
        List<RepositoryMetrics> metrics = rawMetrics.stream()
                .map(row -> new RepositoryMetrics(
                        (String) row[0], // repository
                        ((Number) row[1]).longValue(), // totalReviews
                        ((Number) row[2]).doubleValue(), // averageScore
                        ((Number) row[3]).longValue(), // criticalIssues
                        ((Number) row[4]).longValue()  // completedReviews
                ))
                .toList();
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Gerar relat�rio executivo
     */
    @PostMapping("/{id}/executive-report")
    public ResponseEntity<ExecutiveReportResponse> generateExecutiveReport(@PathVariable Long id) {
        log.info("Gerando relat�rio executivo para revis�o: {}", id);
        
        try {
            Optional<CodeReview> reviewOpt = codeReviewRepository.findById(id);
            if (reviewOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CodeReview review = reviewOpt.get();
            String report = sinqiaAiService.generateExecutiveReport(
                    review.getFileAnalyses(),
                    review.getProjectName(),
                    review.getCommitSha()
            );
            
            return ResponseEntity.ok(new ExecutiveReportResponse(
                    true,
                    "Relat�rio gerado com sucesso",
                    report,
                    LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Erro ao gerar relat�rio executivo para revis�o {}: {}", id, e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new ExecutiveReportResponse(
                            false,
                            "Erro ao gerar relat�rio: " + e.getMessage(),
                            null,
                            LocalDateTime.now()
                    ));
        }
    }

    /**
     * Reexecutar an�lise de uma revis�o existente
     */
    @PostMapping("/{id}/reanalyze")
    public ResponseEntity<AnalysisResponse> reanalyzeCodeReview(@PathVariable Long id) {
        log.info("Reexecutando an�lise para revis�o: {}", id);
        
        try {
            Optional<CodeReview> existingReview = codeReviewRepository.findById(id);
            if (existingReview.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CodeReview review = existingReview.get();
            
            // Extrair arquivos Java da an�lise anterior
            List<String> javaFiles = review.getFileAnalyses().stream()
                    .map(FileAnalysis::getFilePath)
                    .toList();
            
            // Reexecutar an�lise
            CodeReview newReview = mayaAnalysisService.analyzeCommit(
                    review.getCommitSha(),
                    review.getRepositoryName(),
                    review.getProjectName(),
                    review.getAuthor(),
                    review.getTitle(),
                    javaFiles,
                    review.getLlmModel()
            );
            
            if (newReview != null) {
                return ResponseEntity.ok(new AnalysisResponse(
                        true,
                        "Rean�lise conclu�da com sucesso",
                        newReview.getId(),
                        newReview.getStatus(),
                        newReview.getAnalysisScore(),
                        newReview.getCriticalIssues(),
                        newReview.getAnalysisDurationMs()
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(new AnalysisResponse(false, "Falha na rean�lise", null, null, null, null, null));
            }
            
        } catch (Exception e) {
            log.error("Erro durante rean�lise da revis�o {}: {}", id, e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new AnalysisResponse(false, "Erro interno: " + e.getMessage(), null, null, null, null, null));
        }
    }

    /**
     * Obter hist�rico de an�lises por autor
     */
    @GetMapping("/author/{author}/history")
    public ResponseEntity<List<CodeReview>> getAuthorHistory(
            @PathVariable String author,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.debug("Buscando hist�rico do autor: {}", author);
        
        Page<CodeReview> reviews = codeReviewRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
        return ResponseEntity.ok(reviews.getContent());
    }

    /**
     * Deletar revis�o de c�digo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCodeReview(@PathVariable Long id) {
        log.info("Deletando revis�o de c�digo: {}", id);
        
        if (codeReviewRepository.existsById(id)) {
            codeReviewRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    // DTOs para requests e responses
    
    public record AnalysisRequest(
            String commitSha,
            String repositoryName,
            String projectName,
            String author,
            String title,
            List<String> javaFiles,
            String llmModel
    ) {}
    
    public record AnalysisResponse(
            boolean success,
            String message,
            Long reviewId,
            CodeReview.ReviewStatus status,
            Double analysisScore,
            Integer criticalIssues,
            Long analysisDurationMs
    ) {}
    
    public record DashboardStats(
            long totalReviews,
            long completedReviews,
            long failedReviews,
            Double averageScore,
            long totalCriticalIssues,
            List<Object[]> topRepositories,
            List<Object[]> topAuthors
    ) {}
    
    public record RepositoryMetrics(
            String repositoryName,
            long totalReviews,
            double averageScore,
            long criticalIssues,
            long completedReviews
    ) {}
    
    public record ExecutiveReportResponse(
            boolean success,
            String message,
            String report,
            LocalDateTime generatedAt
    ) {}
}
