package com.sinqia.maya.controller;

import com.sinqia.maya.entity.CodeReview;
import com.sinqia.maya.service.TfsService;
import com.sinqia.maya.service.TfsService.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para integração com Azure DevOps/TFS.
 * 
 * Endpoints para:
 * - Conectividade com TFS
 * - Buscar repositórios e commits
 * - Agendar análises automáticas
 * - Sincronização de dados
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/tfs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class TfsController {

    private final TfsService tfsService;

    /**
     * Testar conectividade com Azure DevOps
     */
    @GetMapping("/test-connection")
    public ResponseEntity<ConnectionTestResponse> testConnection() {
        log.info("Testando conectividade com Azure DevOps");
        
        try {
            boolean connected = tfsService.testConnection();
            
            return ResponseEntity.ok(new ConnectionTestResponse(
                    connected,
                    connected ? "Conectado com sucesso" : "Falha na conexão",
                    System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Erro no teste de conectividade: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new ConnectionTestResponse(false, "Erro: " + e.getMessage(), System.currentTimeMillis()));
        }
    }

    /**
     * Listar repositórios de um projeto
     */
    @GetMapping("/projects/{projectName}/repositories")
    public ResponseEntity<List<TfsRepositoryInfo>> getProjectRepositories(@PathVariable String projectName) {
        log.info("Buscando repositórios do projeto: {}", projectName);
        
        try {
            List<TfsRepositoryInfo> repositories = tfsService.getProjectRepositories(projectName);
            return ResponseEntity.ok(repositories);
            
        } catch (Exception e) {
            log.error("Erro ao buscar repositórios do projeto {}: {}", projectName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obter informações de um commit específico
     */
    @GetMapping("/projects/{projectName}/repositories/{repositoryName}/commits/{commitSha}")
    public ResponseEntity<TfsCommitInfo> getCommitInfo(
            @PathVariable String projectName,
            @PathVariable String repositoryName,
            @PathVariable String commitSha) {
        
        log.debug("Buscando informações do commit: {} no repositório: {}/{}", commitSha, projectName, repositoryName);
        
        try {
            TfsCommitInfo commitInfo = tfsService.getCommitInfo(projectName, repositoryName, commitSha);
            
            if (commitInfo != null) {
                return ResponseEntity.ok(commitInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar commit {}: {}", commitSha, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Listar arquivos modificados em um commit
     */
    @GetMapping("/projects/{projectName}/repositories/{repositoryName}/commits/{commitSha}/files")
    public ResponseEntity<CommitFilesResponse> getCommitFiles(
            @PathVariable String projectName,
            @PathVariable String repositoryName,
            @PathVariable String commitSha) {
        
        log.debug("Buscando arquivos modificados no commit: {}", commitSha);
        
        try {
            List<String> files = tfsService.getCommitChangedFiles(projectName, repositoryName, commitSha);
            
            // Filtrar apenas arquivos Java
            List<String> javaFiles = files.stream()
                    .filter(file -> file.endsWith(".java"))
                    .toList();
            
            return ResponseEntity.ok(new CommitFilesResponse(
                    files.size(),
                    javaFiles.size(),
                    files,
                    javaFiles
            ));
            
        } catch (Exception e) {
            log.error("Erro ao buscar arquivos do commit {}: {}", commitSha, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obter conteúdo de um arquivo específico em um commit
     */
    @GetMapping("/projects/{projectName}/repositories/{repositoryName}/commits/{commitSha}/files/content")
    public ResponseEntity<FileContentResponse> getFileContent(
            @PathVariable String projectName,
            @PathVariable String repositoryName,
            @PathVariable String commitSha,
            @RequestParam String filePath) {
        
        log.debug("Buscando conteúdo do arquivo: {} no commit: {}", filePath, commitSha);
        
        try {
            String content = tfsService.getFileContent(projectName, repositoryName, commitSha, filePath);
            
            if (content != null) {
                return ResponseEntity.ok(new FileContentResponse(
                        filePath,
                        content,
                        content.split("\\n").length,
                        content.length()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar conteúdo do arquivo {} no commit {}: {}", filePath, commitSha, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Agendar análise automática de um commit
     */
    @PostMapping("/projects/{projectName}/repositories/{repositoryName}/commits/{commitSha}/analyze")
    public ResponseEntity<ScheduleAnalysisResponse> scheduleCommitAnalysis(
            @PathVariable String projectName,
            @PathVariable String repositoryName,
            @PathVariable String commitSha) {
        
        log.info("Agendando análise automática para commit: {} em {}/{}", commitSha, projectName, repositoryName);
        
        try {
            CodeReview review = tfsService.scheduleCommitAnalysis(projectName, repositoryName, commitSha);
            
            if (review != null) {
                return ResponseEntity.ok(new ScheduleAnalysisResponse(
                        true,
                        "Análise agendada com sucesso",
                        review.getId(),
                        review.getStatus()
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ScheduleAnalysisResponse(
                                false,
                                "Não foi possível agendar a análise. Verifique se o commit existe e contém arquivos Java.",
                                null,
                                null
                        ));
            }
            
        } catch (Exception e) {
            log.error("Erro ao agendar análise para commit {}: {}", commitSha, e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new ScheduleAnalysisResponse(
                            false,
                            "Erro interno: " + e.getMessage(),
                            null,
                            null
                    ));
        }
    }

    /**
     * Buscar pull requests recentes para análise automática
     */
    @GetMapping("/projects/{projectName}/repositories/{repositoryName}/pull-requests")
    public ResponseEntity<List<TfsPullRequestInfo>> getRecentPullRequests(
            @PathVariable String projectName,
            @PathVariable String repositoryName,
            @RequestParam(defaultValue = "7") int days) {
        
        log.info("Buscando pull requests dos últimos {} dias para: {}/{}", days, projectName, repositoryName);
        
        try {
            List<TfsPullRequestInfo> pullRequests = tfsService.getRecentPullRequests(projectName, repositoryName, days);
            return ResponseEntity.ok(pullRequests);
            
        } catch (Exception e) {
            log.error("Erro ao buscar pull requests para {}/{}: {}", projectName, repositoryName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Analisar múltiplos commits de pull requests recentes
     */
    @PostMapping("/projects/{projectName}/repositories/{repositoryName}/analyze-recent")
    public ResponseEntity<BulkAnalysisResponse> analyzeRecentPullRequests(
            @PathVariable String projectName,
            @PathVariable String repositoryName,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int maxAnalyses) {
        
        log.info("Analisando pull requests recentes para: {}/{} (últimos {} dias, máximo {})", 
                projectName, repositoryName, days, maxAnalyses);
        
        try {
            List<TfsPullRequestInfo> pullRequests = tfsService.getRecentPullRequests(projectName, repositoryName, days);
            
            int successCount = 0;
            int failureCount = 0;
            int processedCount = 0;
            
            for (TfsPullRequestInfo pr : pullRequests) {
                if (processedCount >= maxAnalyses) {
                    break;
                }
                
                try {
                    CodeReview review = tfsService.scheduleCommitAnalysis(projectName, repositoryName, pr.lastCommitId());
                    
                    if (review != null) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                    
                } catch (Exception e) {
                    log.warn("Falha ao analisar PR {}: {}", pr.pullRequestId(), e.getMessage());
                    failureCount++;
                }
                
                processedCount++;
            }
            
            return ResponseEntity.ok(new BulkAnalysisResponse(
                    processedCount,
                    successCount,
                    failureCount,
                    String.format("Processados %d pull requests: %d sucessos, %d falhas", 
                            processedCount, successCount, failureCount)
            ));
            
        } catch (Exception e) {
            log.error("Erro durante análise em lote: {}", e.getMessage());
            
            return ResponseEntity.internalServerError()
                    .body(new BulkAnalysisResponse(0, 0, 0, "Erro interno: " + e.getMessage()));
        }
    }

    // DTOs para responses
    
    public record ConnectionTestResponse(
            boolean connected,
            String message,
            long timestamp
    ) {}
    
    public record CommitFilesResponse(
            int totalFiles,
            int javaFiles,
            List<String> allFiles,
            List<String> javaFilesList
    ) {}
    
    public record FileContentResponse(
            String filePath,
            String content,
            int lineCount,
            int characterCount
    ) {}
    
    public record ScheduleAnalysisResponse(
            boolean success,
            String message,
            Long reviewId,
            CodeReview.ReviewStatus status
    ) {}
    
    public record BulkAnalysisResponse(
            int processedCount,
            int successCount,
            int failureCount,
            String message
    ) {}
}
