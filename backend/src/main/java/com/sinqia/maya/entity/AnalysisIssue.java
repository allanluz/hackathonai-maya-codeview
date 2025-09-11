package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade representando um issue encontrado durante a análise MAYA.
 * 
 * Issues são problemas específicos detectados no código, como vazamentos
 * de conexão, violações de padrões, problemas de segurança, etc.
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
     * Análise de arquivo associada
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
     * Título do issue
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Descrição detalhada do issue
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Número da linha onde o issue foi encontrado
     */
    @Column(name = "line_number")
    private Integer lineNumber;

    /**
     * Número da coluna onde o issue foi encontrado
     */
    @Column(name = "column_number")
    private Integer columnNumber;

    /**
     * Sugestão de correção
     */
    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    /**
     * Trecho de código relacionado ao issue
     */
    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    /**
     * Regra violada (se aplicável)
     */
    @Column(name = "rule_violated", length = 100)
    private String ruleViolated;

    /**
     * Indica se o issue pode ser corrigido automaticamente
     */
    @Column(name = "auto_fixable")
    private Boolean autoFixable = false;

    /**
     * Data de criação
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Níveis de severidade dos issues
     */
    public enum IssueSeverity {
        INFO("Info", "Informativo", "#17a2b8", 1),
        WARNING("Warning", "Aviso", "#ffc107", 2),
        ERROR("Error", "Erro", "#fd7e14", 3),
        CRITICAL("Critical", "Crítico", "#dc3545", 4);

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
        CONNECTION_LEAK("Connection Leak", "Vazamento de conexão empresta/devolve"),
        CODE_QUALITY("Code Quality", "Problema de qualidade de código"),
        SECURITY_ISSUE("Security Issue", "Problema de segurança"),
        PERFORMANCE_ISSUE("Performance Issue", "Problema de performance"),
        STYLE_VIOLATION("Style Violation", "Violação de padrão de código"),
        TYPE_CHANGE("Type Change", "Mudança em tipos de dados"),
        METHOD_CHANGE("Method Change", "Mudança em assinatura de método"),
        VALIDATION_CHANGE("Validation Change", "Mudança em validações"),
        COMPLEXITY("Complexity", "Alta complexidade ciclomática"),
        ARCHITECTURE("Architecture", "Violação de padrão arquitetural"),
        DOCUMENTATION("Documentation", "Problema de documentação"),
        NAMING_CONVENTION("Naming Convention", "Violação de convenção de nomenclatura"),
        SQL_INJECTION("SQL Injection", "Possível vulnerabilidade SQL Injection"),
        SENSITIVE_DATA("Sensitive Data", "Exposição de dados sensíveis"),
        EXCEPTION_HANDLING("Exception Handling", "Problema no tratamento de exceções"),
        RESOURCE_LEAK("Resource Leak", "Vazamento de recursos"),
        DEAD_CODE("Dead Code", "Código morto/não utilizado"),
        DUPLICATE_CODE("Duplicate Code", "Código duplicado");

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
     * Constructor padrão
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
     * Constructor completo para criação rápida
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
     * Obter ícone baseado no tipo
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
     * Verificar se é issue crítico relacionado a conexões
     */
    public boolean isCriticalConnectionIssue() {
        return severity == IssueSeverity.CRITICAL && 
               (type == IssueType.CONNECTION_LEAK || type == IssueType.RESOURCE_LEAK);
    }

    /**
     * Obter posição formatada
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
     * Criar issue de vazamento de conexão
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
            "Considere refatorar este método em métodos menores e mais focados"
        );
    }

    /**
     * Criar issue de segurança
     */
    public static AnalysisIssue createSecurityIssue(IssueType securityType, String title, 
                                                   String description, Integer lineNumber) {
        return new AnalysisIssue(
            securityType,
            IssueSeverity.ERROR,
            title,
            description,
            lineNumber,
            "Revise este código para eliminar possíveis vulnerabilidades de segurança"
        );
    }
}
