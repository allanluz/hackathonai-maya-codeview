package com.sinqia.maya.service;

import com.sinqia.maya.entity.CodeReview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Servi�o de integra��o com Azure DevOps/TFS.
 * 
 * Respons�vel por:
 * - Conectar com Azure DevOps REST API
 * - Buscar informa��es de commits e pull requests
 * - Obter arquivos modificados
 * - Agendar an�lises autom�ticas
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TfsService {

    private final RestTemplate restTemplate;
    private final MayaAnalysisService mayaAnalysisService;

    @Value("${maya.tfs.base-url:https://dev.azure.com/sinqia}")
    private String tfsBaseUrl;

    @Value("${maya.tfs.personal-access-token:demo-token}")
    private String personalAccessToken;

    @Value("${maya.tfs.organization:sinqia}")
    private String organization;

    @Value("${maya.tfs.api-version:7.0}")
    private String apiVersion;

    /**
     * Configurar headers de autentica��o para Azure DevOps
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // Autentica��o Basic com PAT
        String auth = ":" + personalAccessToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        return headers;
    }

    /**
     * Buscar informa��es de um commit espec�fico
     */
    public TfsCommitInfo getCommitInfo(String projectName, String repositoryName, String commitSha) {
        log.info("Buscando informa��es do commit: {} no reposit�rio: {}", commitSha, repositoryName);
        
        try {
            String url = String.format("%s/%s/_apis/git/repositories/%s/commits/%s?api-version=%s",
                    tfsBaseUrl, projectName, repositoryName, commitSha, apiVersion);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseCommitInfo(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar commit {}: {}", commitSha, e.getMessage());
        }
        
        return null;
    }

    /**
     * Buscar arquivos modificados em um commit
     */
    public List<String> getCommitChangedFiles(String projectName, String repositoryName, String commitSha) {
        log.debug("Buscando arquivos modificados no commit: {}", commitSha);
        
        try {
            String url = String.format("%s/%s/_apis/git/repositories/%s/commits/%s/changes?api-version=%s",
                    tfsBaseUrl, projectName, repositoryName, commitSha, apiVersion);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseChangedFiles(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar arquivos modificados do commit {}: {}", commitSha, e.getMessage());
        }
        
        return Collections.emptyList();
    }

    /**
     * Buscar conte�do de um arquivo em um commit espec�fico
     */
    public String getFileContent(String projectName, String repositoryName, String commitSha, String filePath) {
        log.debug("Buscando conte�do do arquivo: {} no commit: {}", filePath, commitSha);
        
        try {
            // Encode do path do arquivo
            String encodedPath = filePath.replace("/", "%2F").replace(" ", "%20");
            
            String url = String.format("%s/%s/_apis/git/repositories/%s/items?path=%s&versionDescriptor.version=%s&versionDescriptor.versionType=commit&api-version=%s",
                    tfsBaseUrl, projectName, repositoryName, encodedPath, commitSha, apiVersion);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar conte�do do arquivo {} no commit {}: {}", filePath, commitSha, e.getMessage());
        }
        
        return null;
    }

    /**
     * Buscar pull requests recentes para an�lise autom�tica
     */
    public List<TfsPullRequestInfo> getRecentPullRequests(String projectName, String repositoryName, int days) {
        log.info("Buscando pull requests dos �ltimos {} dias para: {}", days, repositoryName);
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            String formattedDate = cutoffDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            String url = String.format("%s/%s/_apis/git/repositories/%s/pullrequests?searchCriteria.status=completed&searchCriteria.minTime=%s&api-version=%s",
                    tfsBaseUrl, projectName, repositoryName, formattedDate, apiVersion);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parsePullRequests(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar pull requests recentes: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }

    /**
     * Agendar an�lise autom�tica de um commit
     */
    public CodeReview scheduleCommitAnalysis(String projectName, String repositoryName, String commitSha) {
        log.info("Agendando an�lise autom�tica para commit: {}", commitSha);
        
        try {
            // Buscar informa��es do commit
            TfsCommitInfo commitInfo = getCommitInfo(projectName, repositoryName, commitSha);
            if (commitInfo == null) {
                log.warn("Commit n�o encontrado: {}", commitSha);
                return null;
            }
            
            // Buscar arquivos modificados (apenas Java)
            List<String> allFiles = getCommitChangedFiles(projectName, repositoryName, commitSha);
            List<String> javaFiles = allFiles.stream()
                    .filter(file -> file.endsWith(".java"))
                    .toList();
            
            if (javaFiles.isEmpty()) {
                log.info("Nenhum arquivo Java encontrado no commit: {}", commitSha);
                return null;
            }
            
            log.info("Encontrados {} arquivos Java para an�lise no commit: {}", javaFiles.size(), commitSha);
            
            // Iniciar an�lise MAYA
            return mayaAnalysisService.analyzeCommit(
                    commitSha,
                    repositoryName,
                    projectName,
                    commitInfo.author(),
                    commitInfo.comment(),
                    javaFiles,
                    "gpt-4"
            );
            
        } catch (Exception e) {
            log.error("Erro ao agendar an�lise para commit {}: {}", commitSha, e.getMessage());
            return null;
        }
    }

    /**
     * Buscar reposit�rios do projeto
     */
    public List<TfsRepositoryInfo> getProjectRepositories(String projectName) {
        log.info("Buscando reposit�rios do projeto: {}", projectName);
        
        try {
            String url = String.format("%s/%s/_apis/git/repositories?api-version=%s",
                    tfsBaseUrl, projectName, apiVersion);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseRepositories(response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro ao buscar reposit�rios do projeto {}: {}", projectName, e.getMessage());
        }
        
        return Collections.emptyList();
    }

    /**
     * Testar conectividade com Azure DevOps
     */
    public boolean testConnection() {
        log.info("Testando conectividade com Azure DevOps: {}", tfsBaseUrl);
        
        try {
            String url = String.format("%s/_apis/projects?api-version=%s", tfsBaseUrl, apiVersion);
            
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            boolean connected = response.getStatusCode() == HttpStatus.OK;
            log.info("Teste de conectividade: {}", connected ? "SUCESSO" : "FALHA");
            
            return connected;
            
        } catch (Exception e) {
            log.error("Erro no teste de conectividade: {}", e.getMessage());
            return false;
        }
    }

    // M�todos de parsing privados
    
    private TfsCommitInfo parseCommitInfo(Map<String, Object> commitData) {
        try {
            Map<String, Object> author = (Map<String, Object>) commitData.get("author");
            String authorName = author != null ? (String) author.get("name") : "Unknown";
            
            String comment = (String) commitData.get("comment");
            String commitId = (String) commitData.get("commitId");
            
            // Parse da data
            String dateStr = (String) commitData.get("author");
            LocalDateTime commitDate = LocalDateTime.now(); // Simplificado
            
            return new TfsCommitInfo(commitId, authorName, comment, commitDate);
            
        } catch (Exception e) {
            log.error("Erro ao fazer parse das informa��es do commit: {}", e.getMessage());
            return null;
        }
    }
    
    private List<String> parseChangedFiles(Map<String, Object> changesData) {
        List<String> files = new ArrayList<>();
        
        try {
            List<Map<String, Object>> changes = (List<Map<String, Object>>) changesData.get("changes");
            
            if (changes != null) {
                for (Map<String, Object> change : changes) {
                    Map<String, Object> item = (Map<String, Object>) change.get("item");
                    if (item != null) {
                        String path = (String) item.get("path");
                        if (path != null && path.endsWith(".java")) {
                            files.add(path);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao fazer parse dos arquivos modificados: {}", e.getMessage());
        }
        
        return files;
    }
    
    private List<TfsPullRequestInfo> parsePullRequests(Map<String, Object> prData) {
        List<TfsPullRequestInfo> pullRequests = new ArrayList<>();
        
        try {
            List<Map<String, Object>> prs = (List<Map<String, Object>>) prData.get("value");
            
            if (prs != null) {
                for (Map<String, Object> pr : prs) {
                    Integer pullRequestId = (Integer) pr.get("pullRequestId");
                    String title = (String) pr.get("title");
                    String status = (String) pr.get("status");
                    
                    Map<String, Object> createdBy = (Map<String, Object>) pr.get("createdBy");
                    String authorName = createdBy != null ? (String) createdBy.get("displayName") : "Unknown";
                    
                    Map<String, Object> lastMergeCommit = (Map<String, Object>) pr.get("lastMergeCommit");
                    String commitId = lastMergeCommit != null ? (String) lastMergeCommit.get("commitId") : null;
                    
                    if (pullRequestId != null && commitId != null) {
                        pullRequests.add(new TfsPullRequestInfo(
                                pullRequestId,
                                title,
                                status,
                                authorName,
                                commitId,
                                LocalDateTime.now() // Simplificado
                        ));
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao fazer parse dos pull requests: {}", e.getMessage());
        }
        
        return pullRequests;
    }
    
    private List<TfsRepositoryInfo> parseRepositories(Map<String, Object> repoData) {
        List<TfsRepositoryInfo> repositories = new ArrayList<>();
        
        try {
            List<Map<String, Object>> repos = (List<Map<String, Object>>) repoData.get("value");
            
            if (repos != null) {
                for (Map<String, Object> repo : repos) {
                    String id = (String) repo.get("id");
                    String name = (String) repo.get("name");
                    String url = (String) repo.get("webUrl");
                    String defaultBranch = (String) repo.get("defaultBranch");
                    
                    Map<String, Object> project = (Map<String, Object>) repo.get("project");
                    String projectName = project != null ? (String) project.get("name") : "";
                    
                    if (id != null && name != null) {
                        repositories.add(new TfsRepositoryInfo(id, name, projectName, url, defaultBranch));
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao fazer parse dos reposit�rios: {}", e.getMessage());
        }
        
        return repositories;
    }

    // Records para DTOs
    
    public record TfsCommitInfo(
            String commitId,
            String author,
            String comment,
            LocalDateTime date
    ) {}
    
    public record TfsPullRequestInfo(
            Integer pullRequestId,
            String title,
            String status,
            String author,
            String lastCommitId,
            LocalDateTime createdDate
    ) {}
    
    public record TfsRepositoryInfo(
            String id,
            String name,
            String project,
            String webUrl,
            String defaultBranch
    ) {}
}
