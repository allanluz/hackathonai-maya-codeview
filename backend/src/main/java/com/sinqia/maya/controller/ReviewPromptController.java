package com.sinqia.maya.controller;

import com.sinqia.maya.entity.AuxiliaryFile;
import com.sinqia.maya.entity.ReviewPrompt;
import com.sinqia.maya.service.ReviewPromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller para gerenciamento de prompts de revisão personalizados
 */
@RestController
@RequestMapping("/api/v1/review-prompts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ReviewPromptController {

    private final ReviewPromptService reviewPromptService;

    /**
     * Listar todos os prompts de revisão
     */
    @GetMapping
    public ResponseEntity<Page<ReviewPrompt>> getAllPrompts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) ReviewPrompt.PromptType type,
            @RequestParam(required = false) Long repositoryId,
            @RequestParam(required = false) Boolean active) {
        
        log.debug("Listando prompts - tipo: {}, repo: {}, ativo: {}", type, repositoryId, active);
        
        Page<ReviewPrompt> prompts = reviewPromptService.findPrompts(type, repositoryId, active, pageable);
        return ResponseEntity.ok(prompts);
    }

    /**
     * Obter prompt específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewPrompt> getPrompt(@PathVariable Long id) {
        log.debug("Buscando prompt: {}", id);
        
        return reviewPromptService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Criar novo prompt de revisão
     */
    @PostMapping
    public ResponseEntity<CreatePromptResponse> createPrompt(@RequestBody CreatePromptRequest request) {
        log.info("Criando novo prompt: {}", request.name());
        
        try {
            ReviewPrompt prompt = reviewPromptService.createPrompt(request);
            
            return ResponseEntity.ok(new CreatePromptResponse(
                    true,
                    "Prompt criado com sucesso",
                    prompt
            ));
            
        } catch (Exception e) {
            log.error("Erro ao criar prompt: {}", e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new CreatePromptResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Atualizar prompt existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewPrompt> updatePrompt(@PathVariable Long id, @RequestBody UpdatePromptRequest request) {
        log.info("Atualizando prompt: {}", id);
        
        try {
            ReviewPrompt updated = reviewPromptService.updatePrompt(id, request);
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao atualizar prompt {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletar prompt
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeletePromptResponse> deletePrompt(@PathVariable Long id) {
        log.info("Deletando prompt: {}", id);
        
        try {
            reviewPromptService.deletePrompt(id);
            
            return ResponseEntity.ok(new DeletePromptResponse(
                    true,
                    "Prompt deletado com sucesso"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao deletar prompt {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new DeletePromptResponse(false, e.getMessage()));
        }
    }

    /**
     * Definir prompt como padrão
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<SetDefaultResponse> setDefaultPrompt(@PathVariable Long id) {
        log.info("Definindo prompt padrão: {}", id);
        
        try {
            reviewPromptService.setDefaultPrompt(id);
            
            return ResponseEntity.ok(new SetDefaultResponse(
                    true,
                    "Prompt definido como padrão"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao definir prompt padrão {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new SetDefaultResponse(false, e.getMessage()));
        }
    }

    /**
     * Testar prompt com código de exemplo
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<TestPromptResponse> testPrompt(
            @PathVariable Long id, 
            @RequestBody TestPromptRequest request) {
        
        log.info("Testando prompt: {}", id);
        
        try {
            String result = reviewPromptService.testPrompt(id, request.sampleCode(), request.filename());
            
            return ResponseEntity.ok(new TestPromptResponse(
                    true,
                    "Teste executado com sucesso",
                    result
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao testar prompt {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new TestPromptResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Obter prompts aplicáveis para um repositório
     */
    @GetMapping("/repository/{repositoryId}/applicable")
    public ResponseEntity<List<ReviewPrompt>> getApplicablePrompts(@PathVariable Long repositoryId) {
        log.debug("Buscando prompts aplicáveis para repositório: {}", repositoryId);
        
        List<ReviewPrompt> prompts = reviewPromptService.findApplicablePrompts(repositoryId);
        return ResponseEntity.ok(prompts);
    }

    /**
     * Clonar prompt existente
     */
    @PostMapping("/{id}/clone")
    public ResponseEntity<CreatePromptResponse> clonePrompt(@PathVariable Long id, @RequestBody ClonePromptRequest request) {
        log.info("Clonando prompt: {} para {}", id, request.newName());
        
        try {
            ReviewPrompt cloned = reviewPromptService.clonePrompt(id, request.newName(), request.description());
            
            return ResponseEntity.ok(new CreatePromptResponse(
                    true,
                    "Prompt clonado com sucesso",
                    cloned
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao clonar prompt {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new CreatePromptResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Upload de arquivo auxiliar para prompt
     */
    @PostMapping("/{id}/auxiliary-files")
    public ResponseEntity<UploadAuxiliaryFileResponse> uploadAuxiliaryFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") AuxiliaryFile.FileType fileType,
            @RequestParam(value = "description", required = false) String description) {
        
        log.info("Upload de arquivo auxiliar para prompt {}: {}", id, file.getOriginalFilename());
        
        try {
            AuxiliaryFile auxiliaryFile = reviewPromptService.uploadAuxiliaryFile(id, file, fileType, description);
            
            return ResponseEntity.ok(new UploadAuxiliaryFileResponse(
                    true,
                    "Arquivo carregado com sucesso",
                    auxiliaryFile
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao fazer upload do arquivo para prompt {}: {}", id, e.getMessage());
            
            return ResponseEntity.badRequest()
                    .body(new UploadAuxiliaryFileResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Listar arquivos auxiliares de um prompt
     */
    @GetMapping("/{id}/auxiliary-files")
    public ResponseEntity<List<AuxiliaryFile>> getAuxiliaryFiles(@PathVariable Long id) {
        log.debug("Listando arquivos auxiliares do prompt: {}", id);
        
        try {
            List<AuxiliaryFile> files = reviewPromptService.getAuxiliaryFiles(id);
            return ResponseEntity.ok(files);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obter prompts mais utilizados
     */
    @GetMapping("/most-used")
    public ResponseEntity<Page<ReviewPrompt>> getMostUsedPrompts(@PageableDefault(size = 10) Pageable pageable) {
        log.debug("Buscando prompts mais utilizados");
        
        Page<ReviewPrompt> prompts = reviewPromptService.findMostUsed(pageable);
        return ResponseEntity.ok(prompts);
    }

    // DTOs

    public record CreatePromptRequest(
            String name,
            String description,
            String promptTemplate,
            String systemInstructions,
            ReviewPrompt.PromptType type,
            Long repositoryId,
            String projectPattern,
            String fileExtensions,
            List<String> focusAreas,
            List<String> severityLevels,
            Integer maxTokens,
            Double temperature
    ) {}

    public record CreatePromptResponse(
            boolean success,
            String message,
            ReviewPrompt prompt
    ) {}

    public record UpdatePromptRequest(
            String name,
            String description,
            String promptTemplate,
            String systemInstructions,
            String projectPattern,
            String fileExtensions,
            List<String> focusAreas,
            List<String> severityLevels,
            Integer maxTokens,
            Double temperature,
            Boolean isActive
    ) {}

    public record DeletePromptResponse(
            boolean success,
            String message
    ) {}

    public record SetDefaultResponse(
            boolean success,
            String message
    ) {}

    public record TestPromptRequest(
            String sampleCode,
            String filename
    ) {}

    public record TestPromptResponse(
            boolean success,
            String message,
            String result
    ) {}

    public record ClonePromptRequest(
            String newName,
            String description
    ) {}

    public record UploadAuxiliaryFileResponse(
            boolean success,
            String message,
            AuxiliaryFile auxiliaryFile
    ) {}
}
