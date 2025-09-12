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
 * Permite armazenar documentos de refer�ncia, templates, padr�es de c�digo,
 * prompts personalizados e outros recursos que auxiliam na an�lise.
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
     * Conte�do do arquivo
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
     * Descri��o do arquivo
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Indica se o arquivo est� ativo
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Vers�o do arquivo
     */
    @Column(name = "version", length = 20)
    private String version = "1.0";

    /**
     * Tags para categoriza��o
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
     * N�mero de vezes que foi usado
     */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    /**
     * Data da �ltima utiliza��o
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Data de cria��o
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Data da �ltima atualiza��o
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Prompt de revis�o associado (opcional)
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
        CODE_PATTERN("Code Pattern", "Padr�es de c�digo de refer�ncia", ".java"),
        CONFIGURATION("Configuration", "Arquivos de configura��o", ".json"),
        DOCUMENTATION("Documentation", "Documenta��o t�cnica", ".md"),
        TEMPLATE("Template", "Templates de relat�rios", ".html"),
        RULESET("Ruleset", "Conjunto de regras de an�lise", ".json"),
        REFERENCE("Reference", "Documentos de refer�ncia", ".pdf");

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
     * Constructor padr�o
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
     * Obter extens�o do arquivo baseada no nome
     */
    public String getFileExtension() {
        if (name == null || !name.contains(".")) {
            return fileType.getDefaultExtension();
        }
        return name.substring(name.lastIndexOf('.'));
    }

    /**
     * Verificar se � arquivo de texto
     */
    public boolean isTextFile() {
        String ext = getFileExtension().toLowerCase();
        return ext.equals(".md") || ext.equals(".txt") || ext.equals(".json") || 
               ext.equals(".java") || ext.equals(".html") || ext.equals(".xml");
    }

    /**
     * Obter preview do conte�do (primeiros 200 caracteres)
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
     * Obter n�mero de linhas do conte�do
     */
    public int getLineCount() {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.split("\n").length;
    }

    /**
     * Verificar se cont�m tag espec�fica
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
     * Atualizar conte�do e metadados
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
     * Criar arquivo padr�o de documenta��o Sinqia
     */
    public static AuxiliaryFile createSinqiaDocumentation() {
        AuxiliaryFile file = new AuxiliaryFile();
        file.setName("padroes-sinqia.md");
        file.setFileType(FileType.DOCUMENTATION);
        file.setDescription("Padr�es de desenvolvimento Sinqia");
        file.setContent("""
            # Padr�es de Desenvolvimento Sinqia
            
            ## Gest�o de Conex�es
            
            ### Regra Fundamental
            - Toda chamada `empresta()` DEVE ter `devolve()` correspondente
            - Usar sempre estrutura try-finally
            
            ### Exemplo Correto
            ```java
            Connection conn = null;
            try {
                conn = ConexaoFactory.empresta();
                // usar conex�o
            } finally {
                if (conn != null) {
                    ConexaoFactory.devolve(conn);
                }
            }
            ```
            
            ## Nomenclatura
            - Classes em PascalCase
            - M�todos e vari�veis em camelCase
            - Constantes em UPPER_SNAKE_CASE
            - Usar nomes em portugu�s (exceto termos t�cnicos)
            
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
     * Criar prompt padr�o para an�lise MAYA
     */
    public static AuxiliaryFile createMayaAnalysisPrompt() {
        AuxiliaryFile file = new AuxiliaryFile();
        file.setName("maya-analysis-prompt.txt");
        file.setFileType(FileType.PROMPT);
        file.setDescription("Prompt padr�o para an�lise MAYA com IA");
        file.setContent("""
            Voc� � um especialista em revis�o de c�digo focado nos padr�es MAYA da Sinqia.
            
            Analise o c�digo fornecido seguindo estas diretrizes:
            
            1. GEST�O DE CONEX�ES (PRIORIDADE M�XIMA):
               - Verificar empresta()/devolve() balanceados
               - Identificar vazamentos de conex�o
               - Validar uso de try-finally
            
            2. PADR�ES SINQIA:
               - Nomenclatura em portugu�s
               - Estrutura de pacotes
               - Annotations apropriadas
            
            3. QUALIDADE DE C�DIGO:
               - Complexidade ciclom�tica
               - C�digo duplicado
               - Padr�es de formata��o
            
            4. SEGURAN�A:
               - SQL injection
               - Dados sens�veis em logs
               - Tratamento de exce��es
            
            Retorne an�lise em formato JSON com severidade, tipo, descri��o e recomenda��o.
            """);
        file.addTag("maya");
        file.addTag("prompt");
        file.addTag("ia");
        
        return file;
    }
}
