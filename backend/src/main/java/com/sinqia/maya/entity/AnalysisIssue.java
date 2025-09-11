package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade representando um issue encontrado durante a an�lise MAYA.
 * 
 * Issues s�o problemas espec�ficos detectados no c�digo, como vazamentos
 * de conex�o, viola��es de padr�es, problemas de seguran�a, etc.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Entity
@Table(name = "analysis_issues", indexes = {
    @Index(name = "idx_file_analysis_id", columnList = "file_analysis_id"),
    @Index(name = "idx_severity", columnList = "severity"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_line_number", columnList = "line_number")
})
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"fileAnalysis"})
@ToString(exclude = {"fileAnalysis"})
public class AnalysisIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * An�lise de arquivo associada
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_analysis_id", nullable = false)
    private FileAnalysis fileAnalysis;

    /**
     * Severidade do issue
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private IssueSeverity severity;

    /**
     * Tipo do issue
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private IssueType type;

    /**
     * T�tulo do issue
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Descri��o detalhada do issue
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * N�mero da linha onde o issue foi encontrado
     */
    @Column(name = "line_number")
    private Integer lineNumber;

    /**
     * N�mero da coluna onde o issue foi encontrado
     */
    @Column(name = "column_number")
    private Integer columnNumber;

    /**
     * Sugest�o de corre��o
     */
    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    /**
     * Trecho de c�digo relacionado ao issue
     */
    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    /**
     * Regra violada (se aplic�vel)
     */
    @Column(name = "rule_violated", length = 100)
    private String ruleViolated;

    /**
     * Indica se o issue pode ser corrigido automaticamente
     */
    @Column(name = "auto_fixable")
    private Boolean autoFixable = false;

    /**
     * Data de cria��o
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * N�veis de severidade dos issues
     */
    public enum IssueSeverity {
        INFO("Info", "Informativo", "#17a2b8", 1),
        WARNING("Warning", "Aviso", "#ffc107", 2),
        ERROR("Error", "Erro", "#fd7e14", 3),
        CRITICAL("Critical", "Cr�tico", "#dc3545", 4);

        private final String code;
        private final String description;
        private final String color;
        private final int priority;

        IssueSeverity(String code, String description, String color, int priority) {
            this.code = code;
            this.description = description;
            this.color = color;
            this.priority = priority;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
        public String getColor() { return color; }
        public int getPriority() { return priority; }
    }

    /**
     * Tipos de issues detectados pelo sistema MAYA
     */
    public enum IssueType {
        CONNECTION_LEAK("Connection Leak", "Vazamento de conex�o empresta/devolve"),
        CODE_QUALITY("Code Quality", "Problema de qualidade de c�digo"),
        SECURITY_ISSUE("Security Issue", "Problema de seguran�a"),
        PERFORMANCE_ISSUE("Performance Issue", "Problema de performance"),
        STYLE_VIOLATION("Style Violation", "Viola��o de padr�o de c�digo"),
        TYPE_CHANGE("Type Change", "Mudan�a em tipos de dados"),
        METHOD_CHANGE("Method Change", "Mudan�a em assinatura de m�todo"),
        VALIDATION_CHANGE("Validation Change", "Mudan�a em valida��es"),
        COMPLEXITY("Complexity", "Alta complexidade ciclom�tica"),
        ARCHITECTURE("Architecture", "Viola��o de padr�o arquitetural"),
        DOCUMENTATION("Documentation", "Problema de documenta��o"),
        NAMING_CONVENTION("Naming Convention", "Viola��o de conven��o de nomenclatura"),
        SQL_INJECTION("SQL Injection", "Poss�vel vulnerabilidade SQL Injection"),
        SENSITIVE_DATA("Sensitive Data", "Exposi��o de dados sens�veis"),
        EXCEPTION_HANDLING("Exception Handling", "Problema no tratamento de exce��es"),
        RESOURCE_LEAK("Resource Leak", "Vazamento de recursos"),
        DEAD_CODE("Dead Code", "C�digo morto/n�o utilizado"),
        DUPLICATE_CODE("Duplicate Code", "C�digo duplicado");

        private final String code;
        private final String description;

        IssueType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Constructor padr�o
     */
    public AnalysisIssue() {}

    /**
     * Constructor com campos principais
     */
    public AnalysisIssue(IssueType type, IssueSeverity severity, String title, String description) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.description = description;
    }

    /**
     * Constructor completo para cria��o r�pida
     */
    public AnalysisIssue(IssueType type, IssueSeverity severity, String title, 
                        String description, Integer lineNumber, String suggestion) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.lineNumber = lineNumber;
        this.suggestion = suggestion;
    }

    /**
     * Obter �cone baseado no tipo
     */
    public String getIcon() {
        return switch (type) {
            case CONNECTION_LEAK, RESOURCE_LEAK -> "??";
            case SECURITY_ISSUE, SQL_INJECTION -> "??";
            case PERFORMANCE_ISSUE -> "?";
            case CODE_QUALITY -> "??";
            case COMPLEXITY -> "??";
            case STYLE_VIOLATION, NAMING_CONVENTION -> "??";
            case ARCHITECTURE -> "???";
            case DOCUMENTATION -> "??";
            case SENSITIVE_DATA -> "??";
            case EXCEPTION_HANDLING -> "??";
            case DEAD_CODE -> "??";
            case DUPLICATE_CODE -> "??";
            default -> "??";
        };
    }

    /**
     * Obter cor baseada na severidade
     */
    public String getSeverityColor() {
        return severity.getColor();
    }

    /**
     * Verificar se � issue cr�tico relacionado a conex�es
     */
    public boolean isCriticalConnectionIssue() {
        return severity == IssueSeverity.CRITICAL && 
               (type == IssueType.CONNECTION_LEAK || type == IssueType.RESOURCE_LEAK);
    }

    /**
     * Obter posi��o formatada
     */
    public String getPosition() {
        if (lineNumber == null) return "N/A";
        if (columnNumber == null) return "Linha " + lineNumber;
        return String.format("Linha %d, Coluna %d", lineNumber, columnNumber);
    }

    /**
     * Obter resumo do issue
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getIcon()).append(" ");
        summary.append(severity.getCode()).append(": ");
        summary.append(title);
        
        if (lineNumber != null) {
            summary.append(" (linha ").append(lineNumber).append(")");
        }
        
        return summary.toString();
    }

    /**
     * Criar issue de vazamento de conex�o
     */
    public static AnalysisIssue createConnectionLeakIssue(String title, String description, 
                                                         Integer lineNumber, String suggestion) {
        return new AnalysisIssue(
            IssueType.CONNECTION_LEAK,
            IssueSeverity.CRITICAL,
            title,
            description,
            lineNumber,
            suggestion
        );
    }

    /**
     * Criar issue de alta complexidade
     */
    public static AnalysisIssue createComplexityIssue(String title, String description, 
                                                     Integer lineNumber) {
        return new AnalysisIssue(
            IssueType.COMPLEXITY,
            IssueSeverity.WARNING,
            title,
            description,
            lineNumber,
            "Considere refatorar este m�todo em m�todos menores e mais focados"
        );
    }

    /**
     * Criar issue de seguran�a
     */
    public static AnalysisIssue createSecurityIssue(IssueType securityType, String title, 
                                                   String description, Integer lineNumber) {
        return new AnalysisIssue(
            securityType,
            IssueSeverity.ERROR,
            title,
            description,
            lineNumber,
            "Revise este c�digo para eliminar poss�veis vulnerabilidades de seguran�a"
        );
    }
}
