package com.sinqia.maya.service;

import com.sinqia.maya.entity.*;
import com.sinqia.maya.repository.CodeReviewRepository;
import com.sinqia.maya.repository.FileAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servi�o principal de an�lise MAYA.
 * 
 * Implementa os algoritmos espec�ficos de detec��o de vazamentos de conex�o,
 * an�lise de complexidade e conformidade com padr�es Sinqia.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MayaAnalysisService {

    private final CodeReviewRepository codeReviewRepository;
    private final FileAnalysisRepository fileAnalysisRepository;

    // Padr�es regex para detec��o MAYA
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("class\\s+(\\w+)");
    private static final Pattern EMPRESTA_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*\\w*[Cc]onexao\\w*\\.empresta\\s*\\(");
    private static final Pattern DEVOLVE_PATTERN = Pattern.compile("\\w*[Cc]onexao\\w*\\.devolve\\s*\\(([^)]+)\\)");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w\\.]+);");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.]+);");

    /**
     * An�lise principal de um commit
     */
    public CodeReview analyzeCommit(String commitSha, String repositoryName, 
                                   String projectName, String author, String title,
                                   List<String> javaFiles, String llmModel) {
        
        log.info("Iniciando an�lise MAYA para commit: {} do reposit�rio: {}", commitSha, repositoryName);
        
        long startTime = System.currentTimeMillis();
        
        // Criar revis�o de c�digo
        CodeReview review = createCodeReview(commitSha, repositoryName, projectName, author, title, llmModel);
        review.setStatus(CodeReview.ReviewStatus.IN_PROGRESS);
        review = codeReviewRepository.save(review);
        
        try {
            // Analisar cada arquivo Java
            for (String filePath : javaFiles) {
                log.debug("Analisando arquivo: {}", filePath);
                
                // Simular conte�do do arquivo para demonstra��o
                String fileContent = generateSampleJavaContent(filePath);
                
                FileAnalysis analysis = analyzeFile(review, filePath, fileContent);
                review.addFileAnalysis(analysis);
            }
            
            // Calcular m�tricas finais
            review.calculateMetrics();
            review.setStatus(CodeReview.ReviewStatus.COMPLETED);
            
            long duration = System.currentTimeMillis() - startTime;
            review.setAnalysisDurationMs(duration);
            
            log.info("An�lise MAYA conclu�da em {}ms. Score: {}, Issues cr�ticos: {}", 
                    duration, review.getAnalysisScore(), review.getCriticalIssues());
            
        } catch (Exception e) {
            log.error("Erro durante an�lise MAYA para commit: {}", commitSha, e);
            review.setStatus(CodeReview.ReviewStatus.FAILED);
            review.setReviewComment("Erro durante an�lise: " + e.getMessage());
        }
        
        return codeReviewRepository.save(review);
    }

    /**
     * An�lise espec�fica MAYA de um arquivo
     */
    private FileAnalysis analyzeFile(CodeReview review, String filePath, String content) {
        long startTime = System.currentTimeMillis();
        
        FileAnalysis analysis = new FileAnalysis();
        analysis.setCodeReview(review);
        analysis.setFilePath(filePath);
        analysis.setClassName(extractClassName(content));
        analysis.setLanguage(detectLanguage(filePath));
        analysis.setLineCount(content.split("\\n").length);
        
        // An�lises espec�ficas MAYA
        analyzeConnections(analysis, content);
        analyzeComplexity(analysis, content);
        analyzeArchitecture(analysis, content);
        analyzeSecurity(analysis, content);
        
        // Gerar issues baseados na an�lise
        createIssuesFromAnalysis(analysis, content);
        
        // Calcular score final
        analysis.calculateScore();
        
        // Gerar relat�rio markdown
        analysis.setMarkdownReport(generateMarkdownReport(analysis, content));
        
        long processingTime = System.currentTimeMillis() - startTime;
        analysis.setProcessingTimeMs(processingTime);
        
        return fileAnalysisRepository.save(analysis);
    }

    /**
     * An�lise de conex�es empresta/devolve (core MAYA)
     */
    private void analyzeConnections(FileAnalysis analysis, String content) {
        log.debug("Analisando conex�es para arquivo: {}", analysis.getFilePath());
        
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        // Contar empresta() e devolve()
        int empresta = countOccurrences(content, EMPRESTA_PATTERN);
        int devolve = countOccurrences(content, DEVOLVE_PATTERN);
        
        analysis.setConnectionEmpresta(empresta);
        analysis.setConnectionDevolve(devolve);
        analysis.setConnectionBalanced(empresta == devolve);
        
        // Calcular desequil�brio
        if (empresta + devolve > 0) {
            double imbalance = Math.abs(empresta - devolve) / (double)(empresta + devolve) * 100;
            analysis.setConnectionImbalance(imbalance);
        }
        
        log.debug("Conex�es analisadas - empresta: {}, devolve: {}, balanceado: {}", 
                empresta, devolve, analysis.getConnectionBalanced());
    }

    /**
     * An�lise de complexidade ciclom�tica
     */
    private void analyzeComplexity(FileAnalysis analysis, String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        // Contar estruturas de controle
        int complexity = 1; // Complexidade base
        complexity += countOccurrences(content, Pattern.compile("\\bif\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\belse\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\bfor\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\bwhile\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\bswitch\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\bcatch\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\b&&\\b"));
        complexity += countOccurrences(content, Pattern.compile("\\b\\|\\|\\b"));
        
        analysis.setComplexityScore((double) complexity);
        
        log.debug("Complexidade ciclom�tica calculada: {} para arquivo: {}", 
                complexity, analysis.getFilePath());
    }

    /**
     * An�lise de padr�es arquiteturais Sinqia
     */
    private void analyzeArchitecture(FileAnalysis analysis, String content) {
        // Verificar estrutura de pacotes Sinqia
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(content);
        if (packageMatcher.find()) {
            String packageName = packageMatcher.group(1);
            
            // Verificar se segue padr�o com.sinqia.*
            if (!packageName.startsWith("com.sinqia")) {
                analysis.setHasValidationChanges(true); // Usar como flag para viola��es arquiteturais
            }
        }
        
        // Verificar nomenclatura de classe
        String className = analysis.getClassName();
        if (className != null) {
            // Verificar sufixos obrigat�rios
            if (analysis.getFilePath().contains("service") && !className.endsWith("Service")) {
                analysis.setHasMethodChanges(true); // Flag para viola��es de nomenclatura
            }
            if (analysis.getFilePath().contains("controller") && !className.endsWith("Controller")) {
                analysis.setHasMethodChanges(true);
            }
        }
    }

    /**
     * An�lise de seguran�a
     */
    private void analyzeSecurity(FileAnalysis analysis, String content) {
        // Detectar poss�vel SQL injection (concatena��o de strings em SQL)
        Pattern sqlConcatPattern = Pattern.compile("(SELECT|INSERT|UPDATE|DELETE).*\\+.*['\"]", Pattern.CASE_INSENSITIVE);
        if (sqlConcatPattern.matcher(content).find()) {
            analysis.setHasTypeChanges(true); // Flag para issues de seguran�a
        }
        
        // Detectar logging de informa��es sens�veis
        Pattern sensitiveLogPattern = Pattern.compile("log\\.(debug|info|warn|error).*(?:senha|password|token|cpf|cnpj)", Pattern.CASE_INSENSITIVE);
        if (sensitiveLogPattern.matcher(content).find()) {
            analysis.setHasTypeChanges(true);
        }
    }

    /**
     * Criar issues baseados na an�lise
     */
    private void createIssuesFromAnalysis(FileAnalysis analysis, String content) {
        List<AnalysisIssue> issues = new ArrayList<>();
        
        // Issue cr�tico: conex�es desbalanceadas
        if (Boolean.FALSE.equals(analysis.getConnectionBalanced())) {
            int difference = Math.abs(analysis.getConnectionEmpresta() - analysis.getConnectionDevolve());
            
            AnalysisIssue.IssueSeverity severity = difference > 5 ? 
                AnalysisIssue.IssueSeverity.CRITICAL : AnalysisIssue.IssueSeverity.ERROR;
            
            AnalysisIssue issue = new AnalysisIssue();
            issue.setFileAnalysis(analysis);
            issue.setType(AnalysisIssue.IssueType.CONNECTION_LEAK);
            issue.setSeverity(severity);
            issue.setTitle("Conex�es Desbalanceadas");
            issue.setDescription(String.format(
                "Conex�es desbalanceadas detectadas: %d empresta() vs %d devolve() (diferen�a: %d). " +
                "Isso pode causar vazamentos de conex�o.",
                analysis.getConnectionEmpresta(), analysis.getConnectionDevolve(), difference));
            issue.setSuggestion("Garanta que toda chamada empresta() tenha devolve() correspondente, " +
                              "preferencialmente em bloco finally.");
            
            // Encontrar linha do primeiro empresta()
            String[] lines = content.split("\\n");
            for (int i = 0; i < lines.length; i++) {
                if (EMPRESTA_PATTERN.matcher(lines[i]).find()) {
                    issue.setLineNumber(i + 1);
                    issue.setCodeSnippet(lines[i].trim());
                    break;
                }
            }
            
            issues.add(issue);
        }
        
        // Issue: alta complexidade
        if (analysis.getComplexityScore() > 15) {
            AnalysisIssue issue = new AnalysisIssue();
            issue.setFileAnalysis(analysis);
            issue.setType(AnalysisIssue.IssueType.COMPLEXITY);
            issue.setSeverity(AnalysisIssue.IssueSeverity.WARNING);
            issue.setTitle("Alta Complexidade Ciclom�tica");
            issue.setDescription(String.format(
                "Alta complexidade ciclom�tica: %.0f (limite recomendado: 15). " +
                "Considere refatorar para reduzir complexidade.",
                analysis.getComplexityScore()));
            issue.setSuggestion("Divida m�todos complexos em m�todos menores e mais focados.");
            issues.add(issue);
        }
        
        // Issue: viola��es arquiteturais
        if (Boolean.TRUE.equals(analysis.getHasValidationChanges())) {
            AnalysisIssue issue = new AnalysisIssue();
            issue.setFileAnalysis(analysis);
            issue.setType(AnalysisIssue.IssueType.ARCHITECTURE);
            issue.setSeverity(AnalysisIssue.IssueSeverity.ERROR);
            issue.setTitle("Viola��o de Padr�o Arquitetural");
            issue.setDescription("Estrutura de pacotes n�o segue o padr�o Sinqia (com.sinqia.*)");
            issue.setSuggestion("Mova a classe para pacote que segue o padr�o: com.sinqia.[produto].[modulo].[camada]");
            issues.add(issue);
        }
        
        // Issue: problemas de seguran�a
        if (Boolean.TRUE.equals(analysis.getHasTypeChanges())) {
            AnalysisIssue issue = new AnalysisIssue();
            issue.setFileAnalysis(analysis);
            issue.setType(AnalysisIssue.IssueType.SECURITY_ISSUE);
            issue.setSeverity(AnalysisIssue.IssueSeverity.ERROR);
            issue.setTitle("Poss�vel Vulnerabilidade de Seguran�a");
            issue.setDescription("Detectado poss�vel SQL injection ou exposi��o de dados sens�veis em logs");
            issue.setSuggestion("Use PreparedStatement para SQL e evite logar informa��es sens�veis");
            issues.add(issue);
        }
        
        // Salvar issues
        for (AnalysisIssue issue : issues) {
            analysis.addIssue(issue);
        }
    }

    /**
     * Gerar relat�rio em Markdown
     */
    private String generateMarkdownReport(FileAnalysis analysis, String content) {
        StringBuilder report = new StringBuilder();
        
        report.append("# Relat�rio MAYA - ").append(analysis.getFileName()).append("\n\n");
        
        // Informa��es b�sicas
        report.append("## ?? Informa��es B�sicas\n\n");
        report.append("- **Arquivo:** `").append(analysis.getFilePath()).append("`\n");
        report.append("- **Classe:** ").append(analysis.getClassName()).append("\n");
        report.append("- **Linguagem:** ").append(analysis.getLanguage()).append("\n");
        report.append("- **Linhas:** ").append(analysis.getLineCount()).append("\n");
        report.append("- **Score:** ").append(String.format("%.1f", analysis.getScore())).append("/100\n\n");
        
        // An�lise de conex�es
        report.append("## ?? An�lise de Conex�es\n\n");
        report.append("- **empresta():** ").append(analysis.getConnectionEmpresta()).append("\n");
        report.append("- **devolve():** ").append(analysis.getConnectionDevolve()).append("\n");
        report.append("- **Balanceado:** ").append(analysis.getConnectionBalanced() ? "? Sim" : "? N�o").append("\n");
        
        if (analysis.getConnectionImbalance() > 0) {
            report.append("- **Desequil�brio:** ").append(String.format("%.1f%%", analysis.getConnectionImbalance())).append("\n");
        }
        report.append("\n");
        
        // M�tricas de qualidade
        report.append("## ?? M�tricas de Qualidade\n\n");
        report.append("- **Complexidade Ciclom�tica:** ").append(String.format("%.0f", analysis.getComplexityScore()));
        
        if (analysis.getComplexityScore() <= 10) {
            report.append(" ? Boa");
        } else if (analysis.getComplexityScore() <= 15) {
            report.append(" ?? M�dia");
        } else {
            report.append(" ? Alta");
        }
        report.append("\n\n");
        
        // Issues encontrados
        if (!analysis.getIssues().isEmpty()) {
            report.append("## ?? Issues Encontrados\n\n");
            
            for (AnalysisIssue issue : analysis.getIssues()) {
                report.append("### ").append(issue.getIcon()).append(" ").append(issue.getTitle()).append("\n\n");
                report.append("- **Severidade:** ").append(issue.getSeverity().getDescription()).append("\n");
                report.append("- **Tipo:** ").append(issue.getType().getDescription()).append("\n");
                
                if (issue.getLineNumber() != null) {
                    report.append("- **Linha:** ").append(issue.getLineNumber()).append("\n");
                }
                
                report.append("- **Descri��o:** ").append(issue.getDescription()).append("\n");
                
                if (issue.getSuggestion() != null) {
                    report.append("- **Sugest�o:** ").append(issue.getSuggestion()).append("\n");
                }
                
                report.append("\n");
            }
        }
        
        // Recomenda��es
        report.append("## ?? Recomenda��es\n\n");
        
        if (Boolean.FALSE.equals(analysis.getConnectionBalanced())) {
            report.append("- ?? **CR�TICO:** Corrija os vazamentos de conex�o identificados\n");
        }
        
        if (analysis.getComplexityScore() > 15) {
            report.append("- ?? Refatore m�todos com alta complexidade\n");
        }
        
        if (analysis.getScore() < 70) {
            report.append("- ?? Melhore a qualidade geral do c�digo\n");
        }
        
        report.append("- ?? Adicione documenta��o adequada\n");
        report.append("- ?? Implemente testes unit�rios\n\n");
        
        report.append("---\n");
        report.append("*Relat�rio gerado automaticamente pelo Sistema MAYA*\n");
        
        return report.toString();
    }

    // M�todos auxiliares
    
    private CodeReview createCodeReview(String commitSha, String repositoryName, 
                                      String projectName, String author, String title, String llmModel) {
        CodeReview review = new CodeReview();
        review.setCommitSha(commitSha);
        review.setRepositoryName(repositoryName);
        review.setProjectName(projectName);
        review.setAuthor(author);
        review.setTitle(title);
        review.setLlmModel(llmModel);
        review.setStatus(CodeReview.ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }
    
    private String extractClassName(String content) {
        if (content == null) return null;
        
        Matcher matcher = CLASS_NAME_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    private String detectLanguage(String filePath) {
        if (filePath == null) return "unknown";
        
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "java" -> "java";
            case "js", "jsx" -> "javascript";
            case "ts", "tsx" -> "typescript";
            case "py" -> "python";
            case "cs" -> "csharp";
            default -> extension;
        };
    }
    
    private int countOccurrences(String content, Pattern pattern) {
        if (content == null) return 0;
        
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    /**
     * Gerar conte�do Java de exemplo para demonstra��o
     */
    private String generateSampleJavaContent(String filePath) {
        String className = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
        
        // Gerar diferentes tipos de problemas baseados no nome do arquivo
        if (filePath.contains("service")) {
            return generateServiceClassWithIssues(className);
        } else if (filePath.contains("dao") || filePath.contains("repository")) {
            return generateDaoClassWithConnectionIssues(className);
        } else {
            return generateSimpleClass(className);
        }
    }
    
    private String generateServiceClassWithIssues(String className) {
        return String.format("""
            package com.sinqia.exemplo.service;
            
            import java.util.List;
            import java.util.ArrayList;
            
            /**
             * Servi�o de exemplo com alguns problemas para demonstra��o MAYA
             */
            public class %s {
                
                private static final String TIMEOUT_PADRAO = "30000";
                
                public List<Usuario> buscarUsuarios(String filtro, int limite, boolean ativo) {
                    List<Usuario> usuarios = new ArrayList<>();
                    
                    // Complexidade alta - muitos ifs aninhados
                    if (filtro != null) {
                        if (filtro.length() > 0) {
                            if (ativo) {
                                if (limite > 0) {
                                    if (limite < 1000) {
                                        for (int i = 0; i < limite; i++) {
                                            if (i %% 2 == 0) {
                                                usuarios.add(new Usuario());
                                            } else {
                                                if (filtro.contains("admin")) {
                                                    usuarios.add(new Usuario());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    return usuarios;
                }
                
                public void salvarUsuario(Usuario usuario) {
                    if (usuario == null) {
                        throw new IllegalArgumentException("Usu�rio n�o pode ser nulo");
                    }
                    // Implementa��o...
                }
            }
            """, className);
    }
    
    private String generateDaoClassWithConnectionIssues(String className) {
        return String.format("""
            package com.sinqia.exemplo.dao;
            
            import java.sql.Connection;
            import java.sql.PreparedStatement;
            import java.sql.ResultSet;
            import java.util.List;
            
            /**
             * DAO com vazamentos de conex�o para demonstra��o
             */
            public class %s {
                
                public Usuario buscarPorId(Long id) {
                    Connection conn = ConexaoFactory.empresta();
                    
                    try {
                        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuario WHERE id = ?");
                        stmt.setLong(1, id);
                        ResultSet rs = stmt.executeQuery();
                        
                        if (rs.next()) {
                            return new Usuario(rs);
                        }
                        
                        return null; // PROBLEMA: return sem devolve()
                        
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao buscar usu�rio", e);
                    }
                    // PROBLEMA: devolve() nunca � chamado
                }
                
                public List<Usuario> buscarTodos() {
                    Connection conn = ConexaoFactory.empresta();
                    Connection conn2 = ConexaoFactory.empresta(); // PROBLEMA: segunda conex�o
                    
                    try {
                        // usar conex�es
                    } finally {
                        ConexaoFactory.devolve(conn); // PROBLEMA: s� devolve uma conex�o
                    }
                }
                
                public void salvarUsuarioCorreto(Usuario usuario) {
                    Connection conn = null;
                    try {
                        conn = ConexaoFactory.empresta();
                        // salvar usu�rio
                    } finally {
                        if (conn != null) {
                            ConexaoFactory.devolve(conn); // CORRETO!
                        }
                    }
                }
            }
            """, className);
    }
    
    private String generateSimpleClass(String className) {
        return String.format("""
            package com.sinqia.exemplo.util;
            
            /**
             * Classe utilit�ria simples
             */
            public class %s {
                
                public String formatarTexto(String texto) {
                    if (texto == null) {
                        return "";
                    }
                    return texto.trim().toLowerCase();
                }
                
                public boolean validarCpf(String cpf) {
                    // Implementa��o b�sica
                    return cpf != null && cpf.length() == 11;
                }
            }
            """, className);
    }
}
