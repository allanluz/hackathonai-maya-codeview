package com.sinqia.maya.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinqia.maya.entity.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Serviço para integração com GitHub API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.api.base-url:https://api.github.com}")
    private String gitHubApiBaseUrl;

    /**
     * Lista repositórios de uma organização
     */
    public List<GitHubRepository> listOrganizationRepositories(String organization, String accessToken) {
        log.info("Listando repositórios da organização: {}", organization);

        try {
            String url = gitHubApiBaseUrl + "/orgs/" + organization + "/repos?per_page=100";
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode repositories = objectMapper.readTree(response.getBody());
                List<GitHubRepository> result = new ArrayList<>();

                for (JsonNode repo : repositories) {
                    result.add(GitHubRepository.fromJson(repo));
                }

                log.info("Encontrados {} repositórios para organização {}", result.size(), organization);
                return result;
            } else {
                log.error("Erro ao listar repositórios: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("Erro ao acessar API do GitHub para organização {}: {}", organization, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtém detalhes de um repositório específico
     */
    public GitHubRepository getRepository(String owner, String repo, String accessToken) {
        log.debug("Obtendo detalhes do repositório: {}/{}", owner, repo);

        try {
            String url = gitHubApiBaseUrl + "/repos/" + owner + "/" + repo;
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode repoNode = objectMapper.readTree(response.getBody());
                return GitHubRepository.fromJson(repoNode);
            } else {
                log.error("Erro ao obter repositório {}/{}: {}", owner, repo, response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("Erro ao acessar repositório {}/{}: {}", owner, repo, e.getMessage());
            return null;
        }
    }

    /**
     * Lista commits de um repositório
     */
    public List<GitHubCommit> listCommits(String owner, String repo, String accessToken, String branch, int limit) {
        log.debug("Listando commits do repositório: {}/{}, branch: {}", owner, repo, branch);

        try {
            String url = gitHubApiBaseUrl + "/repos/" + owner + "/" + repo + "/commits?sha=" + branch + "&per_page=" + limit;
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode commits = objectMapper.readTree(response.getBody());
                List<GitHubCommit> result = new ArrayList<>();

                for (JsonNode commit : commits) {
                    result.add(GitHubCommit.fromJson(commit));
                }

                log.debug("Encontrados {} commits para {}/{}", result.size(), owner, repo);
                return result;
            } else {
                log.error("Erro ao listar commits: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("Erro ao listar commits de {}/{}: {}", owner, repo, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtém arquivos modificados em um commit
     */
    public List<GitHubCommitFile> getCommitFiles(String owner, String repo, String sha, String accessToken) {
        log.debug("Obtendo arquivos do commit: {} em {}/{}", sha, owner, repo);

        try {
            String url = gitHubApiBaseUrl + "/repos/" + owner + "/" + repo + "/commits/" + sha;
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode commitData = objectMapper.readTree(response.getBody());
                JsonNode files = commitData.get("files");
                List<GitHubCommitFile> result = new ArrayList<>();

                if (files != null && files.isArray()) {
                    for (JsonNode file : files) {
                        result.add(GitHubCommitFile.fromJson(file));
                    }
                }

                log.debug("Encontrados {} arquivos modificados no commit {}", result.size(), sha);
                return result;
            } else {
                log.error("Erro ao obter arquivos do commit: {}", response.getStatusCode());
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("Erro ao obter arquivos do commit {} em {}/{}: {}", sha, owner, repo, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Valida token de acesso
     */
    public boolean validateAccessToken(String accessToken) {
        log.debug("Validando token de acesso GitHub");

        try {
            String url = gitHubApiBaseUrl + "/user";
            HttpHeaders headers = createAuthHeaders(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            boolean isValid = response.getStatusCode().is2xxSuccessful();
            log.debug("Token GitHub válido: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("Erro ao validar token GitHub: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "MAYA-CodeReview-System");
        return headers;
    }

    // DTOs para GitHub

    public record GitHubRepository(
            Long id,
            String name,
            String fullName,
            String description,
            String htmlUrl,
            String cloneUrl,
            String defaultBranch,
            boolean isPrivate,
            String language,
            int size,
            int stargazersCount,
            int forksCount,
            String createdAt,
            String updatedAt
    ) {
        public static GitHubRepository fromJson(JsonNode node) {
            return new GitHubRepository(
                    node.get("id").asLong(),
                    node.get("name").asText(),
                    node.get("full_name").asText(),
                    node.has("description") && !node.get("description").isNull() ? node.get("description").asText() : "",
                    node.get("html_url").asText(),
                    node.get("clone_url").asText(),
                    node.get("default_branch").asText(),
                    node.get("private").asBoolean(),
                    node.has("language") && !node.get("language").isNull() ? node.get("language").asText() : "",
                    node.get("size").asInt(),
                    node.get("stargazers_count").asInt(),
                    node.get("forks_count").asInt(),
                    node.get("created_at").asText(),
                    node.get("updated_at").asText()
            );
        }
    }

    public record GitHubCommit(
            String sha,
            String message,
            String authorName,
            String authorEmail,
            String authorDate,
            String committerName,
            String committerEmail,
            String committerDate,
            String url
    ) {
        public static GitHubCommit fromJson(JsonNode node) {
            JsonNode commit = node.get("commit");
            JsonNode author = commit.get("author");
            JsonNode committer = commit.get("committer");
            
            return new GitHubCommit(
                    node.get("sha").asText(),
                    commit.get("message").asText(),
                    author.get("name").asText(),
                    author.get("email").asText(),
                    author.get("date").asText(),
                    committer.get("name").asText(),
                    committer.get("email").asText(),
                    committer.get("date").asText(),
                    node.get("html_url").asText()
            );
        }
    }

    public record GitHubCommitFile(
            String filename,
            int additions,
            int deletions,
            int changes,
            String status,
            String rawUrl,
            String patch
    ) {
        public static GitHubCommitFile fromJson(JsonNode node) {
            return new GitHubCommitFile(
                    node.get("filename").asText(),
                    node.get("additions").asInt(),
                    node.get("deletions").asInt(),
                    node.get("changes").asInt(),
                    node.get("status").asText(),
                    node.has("raw_url") ? node.get("raw_url").asText() : "",
                    node.has("patch") ? node.get("patch").asText() : ""
            );
        }
    }
}
