package com.sinqia.maya.service;

import com.sinqia.maya.controller.ReviewPromptController;
import com.sinqia.maya.entity.AuxiliaryFile;
import com.sinqia.maya.entity.ReviewPrompt;
import com.sinqia.maya.repository.ReviewPromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de prompts de revisão
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReviewPromptService {

    private final ReviewPromptRepository reviewPromptRepository;
    private final SinqiaAiService sinqiaAiService;

    /**
     * Buscar prompts com filtros
     */
    public Page<ReviewPrompt> findPrompts(ReviewPrompt.PromptType type, Long repositoryId, Boolean active, Pageable pageable) {
        // Implementação simplificada - TODO: adicionar filtros específicos
        if (active == null || active) {
            return reviewPromptRepository.findByIsActiveTrueOrderByNameAsc(pageable);
        } else {
            return reviewPromptRepository.findAll(pageable);
        }
    }

    /**
     * Buscar prompt por ID
     */
    public Optional<ReviewPrompt> findById(Long id) {
        return reviewPromptRepository.findById(id);
    }

    /**
     * Criar novo prompt
     */
    public ReviewPrompt createPrompt(ReviewPromptController.CreatePromptRequest request) {
        log.info("Criando prompt: {}", request.name());

        // Verificar se nome já existe
        if (reviewPromptRepository.existsByNameAndIsActiveTrue(request.name())) {
            throw new IllegalArgumentException("Já existe um prompt ativo com este nome");
        }

        ReviewPrompt prompt = new ReviewPrompt();
        prompt.setName(request.name());
        prompt.setDescription(request.description());
        prompt.setPromptTemplate(request.promptTemplate());
        prompt.setSystemInstructions(request.systemInstructions());
        prompt.setType(request.type());
        prompt.setProjectPattern(request.projectPattern());
        prompt.setFileExtensions(request.fileExtensions());
        prompt.setMaxTokens(request.maxTokens() != null ? request.maxTokens() : 4000);
        prompt.setTemperature(request.temperature() != null ? request.temperature() : 0.3);
        prompt.setIsActive(true);
        prompt.setIsDefault(false);

        // TODO: Converter focusAreas e severityLevels para JSON
        if (request.focusAreas() != null) {
            prompt.setFocusAreas(String.join(",", request.focusAreas()));
        }
        if (request.severityLevels() != null) {
            prompt.setSeverityLevels(String.join(",", request.severityLevels()));
        }

        prompt = reviewPromptRepository.save(prompt);
        log.info("Prompt criado com sucesso: {}", prompt.getId());

        return prompt;
    }

    /**
     * Atualizar prompt
     */
    public ReviewPrompt updatePrompt(Long id, ReviewPromptController.UpdatePromptRequest request) {
        ReviewPrompt prompt = reviewPromptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        if (request.name() != null) {
            prompt.setName(request.name());
        }
        if (request.description() != null) {
            prompt.setDescription(request.description());
        }
        if (request.promptTemplate() != null) {
            prompt.setPromptTemplate(request.promptTemplate());
        }
        if (request.systemInstructions() != null) {
            prompt.setSystemInstructions(request.systemInstructions());
        }
        if (request.projectPattern() != null) {
            prompt.setProjectPattern(request.projectPattern());
        }
        if (request.fileExtensions() != null) {
            prompt.setFileExtensions(request.fileExtensions());
        }
        if (request.maxTokens() != null) {
            prompt.setMaxTokens(request.maxTokens());
        }
        if (request.temperature() != null) {
            prompt.setTemperature(request.temperature());
        }
        if (request.isActive() != null) {
            prompt.setIsActive(request.isActive());
        }

        // TODO: Atualizar focusAreas e severityLevels
        if (request.focusAreas() != null) {
            prompt.setFocusAreas(String.join(",", request.focusAreas()));
        }
        if (request.severityLevels() != null) {
            prompt.setSeverityLevels(String.join(",", request.severityLevels()));
        }

        prompt = reviewPromptRepository.save(prompt);
        log.info("Prompt atualizado: {}", id);

        return prompt;
    }

    /**
     * Deletar prompt
     */
    public void deletePrompt(Long id) {
        ReviewPrompt prompt = reviewPromptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        prompt.setIsActive(false);
        reviewPromptRepository.save(prompt);
        
        log.info("Prompt desativado: {}", id);
    }

    /**
     * Definir prompt como padrão
     */
    public void setDefaultPrompt(Long id) {
        ReviewPrompt prompt = reviewPromptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        // Remover padrão atual
        Optional<ReviewPrompt> currentDefault = reviewPromptRepository.findByIsDefaultTrueAndIsActiveTrue();
        if (currentDefault.isPresent()) {
            currentDefault.get().setIsDefault(false);
            reviewPromptRepository.save(currentDefault.get());
        }

        // Definir novo padrão
        prompt.setIsDefault(true);
        reviewPromptRepository.save(prompt);

        log.info("Prompt definido como padrão: {}", id);
    }

    /**
     * Testar prompt com código de exemplo
     */
    public String testPrompt(Long id, String sampleCode, String filename) {
        ReviewPrompt prompt = reviewPromptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        try {
            // Usar o serviço de IA para testar o prompt
            return sinqiaAiService.testPrompt(prompt.getPromptTemplate(), sampleCode, filename);
            
        } catch (Exception e) {
            log.error("Erro ao testar prompt {}: {}", id, e.getMessage());
            throw new RuntimeException("Falha no teste do prompt: " + e.getMessage());
        }
    }

    /**
     * Buscar prompts aplicáveis para um repositório
     */
    public List<ReviewPrompt> findApplicablePrompts(Long repositoryId) {
        return reviewPromptRepository.findApplicablePrompts(repositoryId);
    }

    /**
     * Clonar prompt
     */
    public ReviewPrompt clonePrompt(Long id, String newName, String description) {
        ReviewPrompt original = reviewPromptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        // Verificar se nome já existe
        if (reviewPromptRepository.existsByNameAndIsActiveTrue(newName)) {
            throw new IllegalArgumentException("Já existe um prompt ativo com este nome");
        }

        ReviewPrompt clone = new ReviewPrompt();
        clone.setName(newName);
        clone.setDescription(description != null ? description : "Clone de: " + original.getName());
        clone.setPromptTemplate(original.getPromptTemplate());
        clone.setSystemInstructions(original.getSystemInstructions());
        clone.setType(original.getType());
        clone.setProjectPattern(original.getProjectPattern());
        clone.setFileExtensions(original.getFileExtensions());
        clone.setFocusAreas(original.getFocusAreas());
        clone.setSeverityLevels(original.getSeverityLevels());
        clone.setMaxTokens(original.getMaxTokens());
        clone.setTemperature(original.getTemperature());
        clone.setIsActive(true);
        clone.setIsDefault(false);

        clone = reviewPromptRepository.save(clone);
        log.info("Prompt clonado: {} -> {}", id, clone.getId());

        return clone;
    }

    /**
     * Upload de arquivo auxiliar
     */
    public AuxiliaryFile uploadAuxiliaryFile(Long promptId, MultipartFile file, AuxiliaryFile.FileType fileType, String description) {
        ReviewPrompt prompt = reviewPromptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        try {
            String content = new String(file.getBytes());
            
            AuxiliaryFile auxiliaryFile = new AuxiliaryFile();
            auxiliaryFile.setName(file.getOriginalFilename());
            auxiliaryFile.setContent(content);
            auxiliaryFile.setFileType(fileType);
            auxiliaryFile.setDescription(description);
            auxiliaryFile.setFileSize((long) file.getSize());
            auxiliaryFile.setMimeType(file.getContentType());
            auxiliaryFile.setReviewPrompt(prompt);
            
            // TODO: Salvar arquivo auxiliar no repositório
            log.info("Arquivo auxiliar carregado para prompt {}: {}", promptId, file.getOriginalFilename());
            
            return auxiliaryFile;
            
        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo para prompt {}: {}", promptId, e.getMessage());
            throw new RuntimeException("Falha no upload do arquivo: " + e.getMessage());
        }
    }

    /**
     * Listar arquivos auxiliares de um prompt
     */
    public List<AuxiliaryFile> getAuxiliaryFiles(Long promptId) {
        ReviewPrompt prompt = reviewPromptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("Prompt não encontrado"));

        return prompt.getAuxiliaryFiles();
    }

    /**
     * Buscar prompts mais utilizados
     */
    public Page<ReviewPrompt> findMostUsed(Pageable pageable) {
        return reviewPromptRepository.findMostUsed(pageable);
    }

    /**
     * Incrementar contador de uso de um prompt
     */
    public void incrementUsage(Long promptId) {
        reviewPromptRepository.findById(promptId).ifPresent(prompt -> {
            prompt.incrementUsage();
            reviewPromptRepository.save(prompt);
        });
    }
}
