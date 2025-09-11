package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade representando a an�lise de um arquivo espec�fico.
 * 
 * Cont�m todas as m�tricas e resultados da an�lise MAYA para um arquivo,
 * incluindo detec��o de vazamentos de conex�o, complexidade e qualidade.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Entity
@Table(name = "file_analyses", indexes = {
    @Index(name = "idx_code_review_id", columnList = "code_review_id"),
    @Index(name = "idx_file_path", columnList = "file_path"),
    @Index(name = "idx_language", columnList = "language"),
    @Index(name = "idx_connection_balanced", columnList = "connection_balanced")
})
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"codeReview"})
@ToString(exclude = {"codeReview", "issues"})
public class FileAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Revis�o de c�digo associada
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_review_id", nullable = false)
    private CodeReview codeReview;

    /**
     * Caminho do arquivo
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * Nome da classe principal do arquivo
     */
    @Column(name = "class_name", length = 200)
    private String className;

    /**
     * Linguagem de programa��o
     */
    @Column(name = "language", length = 50)
    private String language;

    /**
     * N�mero de linhas do arquivo
     */
    @Column(name = "line_count")
    private Integer lineCount = 0;

    /**
     * Score de complexidade ciclom�tica
     */
    @Column(name = "complexity_score")
    private Double complexityScore = 0.0;

    /**
     * Desequil�brio de conex�es (percentual)
     */
    @Column(name = "connection_imbalance")
    private Double connectionImbalance = 0.0;

    /**
     * Score geral do arquivo (0-100)
     */
    @Column(name = "score")
    private Double score = 0.0;

    // === Campos espec�ficos MAYA ===

    /**
     * N�mero de chamadas empresta() encontradas
     */
    @Column(name = "connection_empresta")
    private Integer connectionEmpresta = 0;

    /**
     * N�mero de chamadas devolve() encontradas
     */
    @Column(name = "connection_devolve")
    private Integer connectionDevolve = 0;

    /**
     * Indica se as conex�es est�o balanceadas
     */
    @Column(name = "connection_balanced")
    private Boolean connectionBalanced = true;

    /**
     * Indica se h� mudan�as em tipos de dados
     */
    @Column(name = "has_type_changes")
    private Boolean hasTypeChanges = false;

    /**
     * Indica se h� mudan�as em m�todos
     */
    @Column(name = "has_method_changes")
    private Boolean hasMethodChanges = false;

    /**
     * Indica se h� mudan�as em valida��es
     */
    @Column(name = "has_validation_changes")
    private Boolean hasValidationChanges = false;

    /**
     * Relat�rio textual da an�lise
     */
    @Column(name = "analysis_report", columnDefinition = "TEXT")
    private String analysisReport;

    /**
     * Relat�rio em formato Markdown
     */
    @Column(name = "markdown_report", columnDefinition = "TEXT")
    private String markdownReport;

    /**
     * Modelo de IA utilizado na an�lise
     */
    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed;

    /**
     * Indica se an�lise com IA foi utilizada
     */
    @Column(name = "ai_analysis_used")
    private Boolean aiAnalysisUsed = false;

    /**
     * Tempo de processamento em milissegundos
     */
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    /**
     * Data de cria��o
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Issues encontrados na an�lise
     */
    @OneToMany(mappedBy = "fileAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AnalysisIssue> issues = new ArrayList<>();

    /**
     * Adicionar issue � an�lise
     */
    public void addIssue(AnalysisIssue issue) {
        issues.add(issue);
        issue.setFileAnalysis(this);
    }

    /**
     * Obter nome do arquivo sem caminho
     */
    public String getFileName() {
        if (filePath == null) return null;
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    /**
     * Obter extens�o do arquivo
     */
    public String getFileExtension() {
        if (filePath == null) return null;
        int lastDot = filePath.lastIndexOf('.');
        return lastDot > 0 ? filePath.substring(lastDot + 1) : "";
    }

    /**
     * Verificar se arquivo � Java
     */
    public boolean isJavaFile() {
        return "java".equalsIgnoreCase(getFileExtension()) || 
               "java".equalsIgnoreCase(language);
    }

    /**
     * Calcular score baseado nas m�tricas
     */
    public void calculateScore() {
        double score = 100.0;

        // Penalizar conex�es desbalanceadas
        if (Boolean.FALSE.equals(connectionBalanced)) {
            int difference = Math.abs(connectionEmpresta - connectionDevolve);
            score -= Math.min(50.0, difference * 10.0);
        }

        // Penalizar alta complexidade
        if (complexityScore != null && complexityScore > 10) {
            score -= Math.min(30.0, (complexityScore - 10) * 2.0);
        }

        // Penalizar issues cr�ticos
        long criticalIssues = issues.stream()
                .filter(issue -> issue.getSeverity() == AnalysisIssue.IssueSeverity.CRITICAL)
                .count();
        score -= criticalIssues * 20.0;

        // Penalizar issues de alta severidade
        long highIssues = issues.stream()
                .filter(issue -> issue.getSeverity() == AnalysisIssue.IssueSeverity.ERROR)
                .count();
        score -= highIssues * 10.0;

        this.score = Math.max(0.0, Math.round(score * 100.0) / 100.0);
    }

    /**
     * Obter severidade geral do arquivo
     */
    public String getOverallSeverity() {
        boolean hasCritical = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == AnalysisIssue.IssueSeverity.CRITICAL);
        
        if (hasCritical) return "CRITICAL";

        boolean hasError = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == AnalysisIssue.IssueSeverity.ERROR);
        
        if (hasError) return "ERROR";

        boolean hasWarning = issues.stream()
                .anyMatch(issue -> issue.getSeverity() == AnalysisIssue.IssueSeverity.WARNING);
        
        if (hasWarning) return "WARNING";

        return issues.isEmpty() ? "NONE" : "INFO";
    }

    /**
     * Obter contagem de issues por severidade
     */
    public long getIssueCountBySeverity(AnalysisIssue.IssueSeverity severity) {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == severity)
                .count();
    }

    /**
     * Verificar se possui vazamentos de conex�o
     */
    public boolean hasConnectionLeaks() {
        return Boolean.FALSE.equals(connectionBalanced) || 
               connectionImbalance > 0;
    }

    /**
     * Obter resumo da an�lise
     */
    public String getAnalysisSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Arquivo: ").append(getFileName()).append("\n");
        summary.append("Linguagem: ").append(language).append("\n");
        summary.append("Linhas: ").append(lineCount).append("\n");
        summary.append("Score: ").append(score).append("/100\n");
        
        if (Boolean.FALSE.equals(connectionBalanced)) {
            summary.append("?? VAZAMENTO DE CONEX�O DETECTADO\n");
            summary.append("  - empresta(): ").append(connectionEmpresta).append("\n");
            summary.append("  - devolve(): ").append(connectionDevolve).append("\n");
        }
        
        summary.append("Issues: ").append(issues.size());
        
        return summary.toString();
    }
}
