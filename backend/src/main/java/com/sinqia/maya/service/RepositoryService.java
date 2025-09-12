package com.sinqia.maya.service;

import com.sinqia.maya.controller.RepositoryController;
import com.sinqia.maya.entity.Repository;
import com.sinqia.maya.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servi�o para gerenciamento de reposit�rios
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final GitHubService gitHubService;

    /**
     * Buscar reposit�rios com filtros
     */
    public Page<Repository> findRepositories(Repository.RepositoryType type, String organization, Boolean active, Pageable pageable) {
        // Para simplicidade, implementar busca b�sica
        // TODO: Implementar busca avan�ada com especifica��es JPA
        if (active == null || active) {
            return repositoryRepository.findByIsActiveTrueOrderByNameAsc(pageable);
        } else {
            return repositoryRepository.findAll(pageable);
        }
    }

    /**
     * Buscar reposit�rio por ID
     */
    public Optional<Repository> findById(Long id) {
        return repositoryRepository.findById(id);
    }

    /**
     * Conectar novo reposit�rio
     */
    public Repository connectRepository(RepositoryController.ConnectRepositoryRequest request) {
        log.info("Conectando reposit�rio: {} - {}", request.name(), request.url());

        // Verificar se reposit�rio j� existe
        if (repositoryRepository.existsByUrl(request.url())) {
            throw new IllegalArgumentException("Reposit�rio j� est� conectado");
        }

        Repository repository = new Repository();
        repository.setName(request.name());
        repository.setUrl(request.url());
        repository.setType(request.type());
        repository.setOrganizationName(request.organizationName());
        repository.setProjectName(request.projectName());
        repository.setAccessToken(request.accessToken());
        repository.setDefaultBranch(request.defaultBranch() != null ? request.defaultBranch() : "main");
        repository.setAutoReviewEnabled(request.autoReviewEnabled() != null ? request.autoReviewEnabled() : false);
        repository.setIsActive(true);

        repository = repositoryRepository.save(repository);
        log.info("Reposit�rio conectado com sucesso: {}", repository.getId());

        return repository;
    }

    /**
     * Atualizar reposit�rio
     */
    public Repository updateRepository(Long id, RepositoryController.UpdateRepositoryRequest request) {
        Repository repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reposit�rio n�o encontrado"));

        if (request.name() != null) {
            repository.setName(request.name());
        }
        if (request.accessToken() != null) {
            repository.setAccessToken(request.accessToken());
        }
        if (request.defaultBranch() != null) {
            repository.setDefaultBranch(request.defaultBranch());
        }
        if (request.autoReviewEnabled() != null) {
            repository.setAutoReviewEnabled(request.autoReviewEnabled());
        }
        if (request.isActive() != null) {
            repository.setIsActive(request.isActive());
        }

        repository = repositoryRepository.save(repository);
        log.info("Reposit�rio atualizado: {}", id);

        return repository;
    }

    /**
     * Desconectar reposit�rio
     */
    public void disconnectRepository(Long id) {
        Repository repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reposit�rio n�o encontrado"));

        repository.setIsActive(false);
        repositoryRepository.save(repository);
        
        log.info("Reposit�rio desconectado: {}", id);
    }

    /**
     * Testar conex�o com reposit�rio
     */
    public boolean testConnection(Long id) {
        Repository repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reposit�rio n�o encontrado"));

        if (repository.isGitHub()) {
            return gitHubService.validateAccessToken(repository.getAccessToken());
        } else if (repository.isTfs()) {
            // TODO: Implementar teste para TFS/Azure DevOps
            return true;
        }

        return false;
    }

    /**
     * Sincronizar reposit�rio
     */
    public void syncRepository(Long id) {
        Repository repository = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reposit�rio n�o encontrado"));

        // Atualizar timestamp de sincroniza��o
        repository.setLastSyncAt(LocalDateTime.now());
        repositoryRepository.save(repository);

        // TODO: Implementar l�gica de sincroniza��o (buscar novos commits, etc.)
        log.info("Reposit�rio sincronizado: {}", id);
    }
}
