package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade para armazenamento de arquivos auxiliares do sistema MAYA.
 * 
 * Permite armazenar documentos de referência, templates, padrões de código,
 * prompts personalizados e outros recursos que auxiliam na análise.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Entity
@Table(name = "auxiliary_files", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_file_type", columnList = "file_type"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class AuxiliaryFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do arquivo
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Conteúdo do arquivo
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * Tipo do arquivo
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private FileType fileType;

    /**
     * Descrição do arquivo
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Indica se o arquivo está ativo
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Versão do arquivo
     */
    @Column(name = "version", length = 20)
    private String version = "1.0";

    /**
     * Tags para categorização
     */
    @Column(name = "tags", length = 200)
    private String tags;

    /**
     * Tamanho do arquivo em bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME type do arquivo
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * Número de vezes que foi usado
     */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    /**
     * Data da última utilização
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Data de criação
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Data da última atualização
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Prompt de revisão associado (opcional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_prompt_id")
    private ReviewPrompt reviewPrompt;

    /**
     * Tipos de arquivos auxiliares suportados
     */
    public enum FileType {
        MARKDOWN("Markdown", "Documentos em formato Markdown", ".md"),
        PROMPT("Prompt", "Templates de prompts para IA", ".txt"),
        CODE_PATTERN("Code Pattern", "Padrões de código de referência", ".java"),
        CONFIGURATION("Configuration", "Arquivos de configuração", ".json"),
        DOCUMENTATION("Documentation", "Documentação técnica", ".md"),
        TEMPLATE("Template", "Templates de relatórios", ".html"),
        RULESET("Ruleset", "Conjunto de regras de análise", ".json"),
        REFERENCE("Reference", "Documentos de referência", ".pdf");

        private final String displayName;
        private final String description;
        private final String defaultExtension;

        FileType(String displayName, String description, String defaultExtension) {
            this.displayName = displayName;
            this.description = description;
            this.defaultExtension = defaultExtension;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getDefaultExtension() { return defaultExtension; }
    }

    /**
     * Constructor padrão
     */
    public AuxiliaryFile() {}

    /**
     * Constructor com campos principais
     */
    public AuxiliaryFile(String name, String content, FileType fileType, String description) {
        this.name = name;
        this.content = content;
        this.fileType = fileType;
        this.description = description;
        this.isActive = true;
        this.version = "1.0";
        
        if (content != null) {
            this.fileSize = (long) content.getBytes().length;
        }
    }

    /**
     * Incrementar contador de uso
     */
    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Obter extensão do arquivo baseada no nome
     */
    public String getFileExtension() {
        if (name == null || !name.contains(".")) {
            return fileType.getDefaultExtension();
        }
        return name.substring(name.lastIndexOf('.'));
    }

    /**
     * Verificar se é arquivo de texto
     */
    public boolean isTextFile() {
        String ext = getFileExtension().toLowerCase();
        return ext.equals(".md") || ext.equals(".txt") || ext.equals(".json") || 
               ext.equals(".java") || ext.equals(".html") || ext.equals(".xml");
    }

    /**
     * Obter preview do conteúdo (primeiros 200 caracteres)
     */
    public String getContentPreview() {
        if (content == null || content.trim().isEmpty()) {
            return "Arquivo vazio";
        }
        
        String preview = content.trim();
        if (preview.length() <= 200) {
            return preview;
        }
        
        return preview.substring(0, 200) + "...";
    }

    /**
     * Obter número de linhas do conteúdo
     */
    public int getLineCount() {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.split("\n").length;
    }

    /**
     * Verificar se contém tag específica
     */
    public boolean hasTag(String tag) {
        if (tags == null || tag == null) {
            return false;
        }
        return tags.toLowerCase().contains(tag.toLowerCase());
    }

    /**
     * Adicionar tag
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        if (tags == null || tags.trim().isEmpty()) {
            tags = tag.trim();
        } else if (!hasTag(tag)) {
            tags = tags + ", " + tag.trim();
        }
    }

    /**
     * Remover tag
     */
    public void removeTag(String tag) {
        if (tags == null || tag == null) {
            return;
        }
        
        tags = tags.replaceAll("(?i),?\\s*" + tag.trim() + "\\s*,?", "");
        tags = tags.replaceAll("^,\\s*|\\s*,$", "").trim();
        
        if (tags.isEmpty()) {
            tags = null;
        }
    }

    /**
     * Atualizar conteúdo e metadados
     */
    public void updateContent(String newContent) {
        this.content = newContent;
        
        if (newContent != null) {
            this.fileSize = (long) newContent.getBytes().length;
        } else {
            this.fileSize = 0L;
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Obter tamanho formatado
     */
    public String getFormattedSize() {
        if (fileSize == null || fileSize == 0) {
            return "0 bytes";
        }
        
        long size = fileSize;
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }

    /**
     * Criar arquivo padrão de documentação Sinqia
     */
    public static AuxiliaryFile createSinqiaDocumentation() {
        AuxiliaryFile file = new AuxiliaryFile();
        file.setName("padroes-sinqia.md");
        file.setFileType(FileType.DOCUMENTATION);
        file.setDescription("Padrões de desenvolvimento Sinqia");
        file.setContent("""
            # Padrões de Desenvolvimento Sinqia
            
            ## Gestão de Conexões
            
            ### Regra Fundamental
            - Toda chamada `empresta()` DEVE ter `devolve()` correspondente
            - Usar sempre estrutura try-finally
            
            ### Exemplo Correto
            ```java
            Connection conn = null;
            try {
                conn = ConexaoFactory.empresta();
                // usar conexão
            } finally {
                if (conn != null) {
                    ConexaoFactory.devolve(conn);
                }
            }
            ```
            
            ## Nomenclatura
            - Classes em PascalCase
            - Métodos e variáveis em camelCase
            - Constantes em UPPER_SNAKE_CASE
            - Usar nomes em português (exceto termos técnicos)
            
            ## Estrutura de Pacotes
            - com.sinqia.[produto].[modulo].[camada]
            - Camadas: dao, dto, service, controller, util
            """);
        file.addTag("sinqia");
        file.addTag("padroes");
        file.addTag("documentacao");
        
        return file;
    }

    /**
     * Criar prompt padrão para análise MAYA
     */
    public static AuxiliaryFile createMayaAnalysisPrompt() {
        AuxiliaryFile file = new AuxiliaryFile();
        file.setName("maya-analysis-prompt.txt");
        file.setFileType(FileType.PROMPT);
        file.setDescription("Prompt padrão para análise MAYA com IA");
        file.setContent("""
            Você é um especialista em revisão de código focado nos padrões MAYA da Sinqia.
            
            Analise o código fornecido seguindo estas diretrizes:
            
            1. GESTÃO DE CONEXÕES (PRIORIDADE MÁXIMA):
               - Verificar empresta()/devolve() balanceados
               - Identificar vazamentos de conexão
               - Validar uso de try-finally
            
            2. PADRÕES SINQIA:
               - Nomenclatura em português
               - Estrutura de pacotes
               - Annotations apropriadas
            
            3. QUALIDADE DE CÓDIGO:
               - Complexidade ciclomática
               - Código duplicado
               - Padrões de formatação
            
            4. SEGURANÇA:
               - SQL injection
               - Dados sensíveis em logs
               - Tratamento de exceções
            
            Retorne análise em formato JSON com severidade, tipo, descrição e recomendação.
            """);
        file.addTag("maya");
        file.addTag("prompt");
        file.addTag("ia");
        
        return file;
    }
}
