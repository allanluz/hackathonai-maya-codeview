package com.sinqia.maya.controller;

import com.sinqia.maya.entity.Repository;
import com.sinqia.maya.service.GitHubService;
import com.sinqia.maya.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de repositórios conectados
 */
@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final GitHubService gitHubService;

    /**
     * Listar todos os repositórios conectados
     */
    @GetMapping
    public ResponseEntity<Page<Repository>> getAllRepositories(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Repository.RepositoryType type,
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) Boolean active) {
        
        log.debug("Listando repositórios - tipo: {}, org: {}, ativo: {}", type, organization, active);
        
        Page<Repository> repositories = repositoryService.findRepositories(type, organization, active, pageable);
        return ResponseEntity.ok(repositories);
    }

    /**
     * Obter detalhes de um repositório específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<Repository> getRepository(@PathVariable Long id) {
        log.debug("Buscando repositório: {}", id);
        
        return repositoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Conectar novo repositório
     */
    @PostMapping
    public ResponseEntity<ConnectRepositoryResponse> connectRepository(@RequestBody ConnectRepositoryRequest request) {
        log.info("Conectando novo repositório: {} - {}", request.type(), request.url());
        
        try {
            // Validar token de acesso
            boolean tokenValid = false;
            if (Repository.RepositoryType.GITHUB.equals(request.type())) {
                tokenValid = gitHubService.validateAccessToken(request.accessToken());
            } else if (Repository.RepositoryType.TFS.equals(request.type()) || 
                       Repository.RepositoryType.AZURE_DEVOPS.equals(request.type())) {
                // TODO: Implementar validação para TFS/Azure DevOps
                tokenValid = true; // Por enquanto, assumir válido
            }

            if (!tokenValid) {
                return ResponseEntity.badRequest()
                        .body(new ConnectRepositoryResponse(false, "Token de acesso inválido", null));
            }

            Repository repository = repositoryService.connectRepository(request);
            
            return ResponseEntity.ok(new ConnectRepositoryResponse(
                    true, 
                    "Repositório conectado com sucesso", 
                    repository
            ));
            
        } catch (Exception e) {
            log.error("Erro ao conectar repositório: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new ConnectRepositoryResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Atualizar configurações de um repositório
     */
    @PutMapping("/{id}")
    public ResponseEntity<Repository> updateRepository(@PathVariable Long id, @RequestBody UpdateRepositoryRequest request) {
        log.info("Atualizando repositório: {}", id);
        
        try {
            Repository updated = repositoryService.updateRepository(id, request);
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao atualizar repositório {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Desconectar repositório
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DisconnectRepositoryResponse> disconnectRepository(@PathVariable Long id) {
        log.info("Desconectando repositório: {}", id);
        
        try {
            repositoryService.disconnectRepository(id);
            
            return ResponseEntity.ok(new DisconnectRepositoryResponse(
                    true, 
                    "Repositório desconectado com sucesso"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao desconectar repositório {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new DisconnectRepositoryResponse(false, e.getMessage()));
        }
    }

    /**
     * Testar conexão com repositório
     */
    @PostMapping("/{id}/test-connection")
    public ResponseEntity<TestConnectionResponse> testConnection(@PathVariable Long id) {
        log.info("Testando conexão com repositório: {}", id);
        
        try {
            boolean connectionValid = repositoryService.testConnection(id);
            
            return ResponseEntity.ok(new TestConnectionResponse(
                    connectionValid,
                    connectionValid ? "Conexão válida" : "Falha na conexão"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao testar conexão do repositório {}: {}", id, e.getMessage());
            
            return ResponseEntity.ok(new TestConnectionResponse(false, e.getMessage()));
        }
    }

    /**
     * Listar repositórios disponíveis de uma organização (GitHub/TFS)
     */
    @PostMapping("/discover")
    public ResponseEntity<DiscoverRepositoriesResponse> discoverRepositories(@RequestBody DiscoverRepositoriesRequest request) {
        log.info("Descobrindo repositórios - tipo: {}, org: {}", request.type(), request.organization());
        
        try {
            List<?> repositories;
            
            if (Repository.RepositoryType.GITHUB.equals(request.type())) {
                repositories = gitHubService.listOrganizationRepositories(request.organization(), request.accessToken());
            } else {
                // TODO: Implementar descoberta para TFS/Azure DevOps
                repositories = List.of();
            }
            
            return ResponseEntity.ok(new DiscoverRepositoriesResponse(
                    true,
                    "Repositórios descobertos com sucesso",
                    repositories.size(),
                    repositories
            ));
            
        } catch (Exception e) {
            log.error("Erro ao descobrir repositórios: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new DiscoverRepositoriesResponse(false, e.getMessage(), 0, List.of()));
        }
    }

    /**
     * Sincronizar repositório com origem
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<SyncRepositoryResponse> syncRepository(@PathVariable Long id) {
        log.info("Sincronizando repositório: {}", id);
        
        try {
            repositoryService.syncRepository(id);
            
            return ResponseEntity.ok(new SyncRepositoryResponse(
                    true,
                    "Repositório sincronizado com sucesso"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao sincronizar repositório {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new SyncRepositoryResponse(false, e.getMessage()));
        }
    }

    // DTOs

    public record ConnectRepositoryRequest(
            String name,
            String url,
            Repository.RepositoryType type,
            String organizationName,
            String projectName,
            String accessToken,
            String defaultBranch,
            Boolean autoReviewEnabled
    ) {}

    public record ConnectRepositoryResponse(
            boolean success,
            String message,
            Repository repository
    ) {}

    public record UpdateRepositoryRequest(
            String name,
            String accessToken,
            String defaultBranch,
            Boolean autoReviewEnabled,
            Boolean isActive
    ) {}

    public record DisconnectRepositoryResponse(
            boolean success,
            String message
    ) {}

    public record TestConnectionResponse(
            boolean success,
            String message
    ) {}

    public record DiscoverRepositoriesRequest(
            Repository.RepositoryType type,
            String organization,
            String accessToken
    ) {}

    public record DiscoverRepositoriesResponse(
            boolean success,
            String message,
            int count,
            List<?> repositories
    ) {}

    public record SyncRepositoryResponse(
            boolean success,
            String message
    ) {}
}
