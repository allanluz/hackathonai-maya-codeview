# MAYA Code Review System - Padrões e Algoritmos MAYA

## 📋 Visão Geral dos Padrões MAYA

O sistema MAYA (Motor de Análise Y Auditoria) implementa algoritmos específicos da Sinqia para detectar problemas comuns em código Java, com foco especial em **vazamentos de conexão** e conformidade com padrões internos.

## 🔍 Algoritmo Principal: Detecção de Connection Leaks

### Conceito Fundamental

O padrão SINQIA para gestão de conexões é baseado em:
- **empresta()**: Método para obter uma conexão do pool
- **devolve()**: Método para devolver a conexão ao pool
- **Regra crítica**: Toda chamada `empresta()` DEVE ter `devolve()` correspondente em bloco `finally`

### Implementação do Detector

```java
/**
 * Serviço principal de análise MAYA
 */
@Service
public class MayaAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(MayaAnalysisService.class);
    
    // Padrões regex para detecção
    private static final Pattern EMPRESTA_PATTERN = Pattern.compile(
        "\\b(\\w+)\\s*=\\s*\\w*[Cc]onexao\\w*\\.empresta\\s*\\(", 
        Pattern.MULTILINE
    );
    
    private static final Pattern DEVOLVE_PATTERN = Pattern.compile(
        "\\w*[Cc]onexao\\w*\\.devolve\\s*\\(([^)]+)\\)",
        Pattern.MULTILINE
    );
    
    private static final Pattern TRY_FINALLY_PATTERN = Pattern.compile(
        "try\\s*\\{[^}]*\\}\\s*finally\\s*\\{[^}]*devolve[^}]*\\}",
        Pattern.DOTALL
    );
    
    /**
     * Análise principal de vazamento de conexões
     */
    public ConnectionLeakAnalysis analyzeConnectionLeaks(String javaCode, String fileName) {
        ConnectionLeakAnalysis analysis = new ConnectionLeakAnalysis();
        analysis.setFileName(fileName);
        analysis.setAnalysisDate(LocalDateTime.now());
        
        // 1. Detectar todas as chamadas empresta()
        List<EmprestaCall> emprestaCalls = findEmprestaCalls(javaCode);
        analysis.setEmprestaCalls(emprestaCalls);
        
        // 2. Detectar todas as chamadas devolve()
        List<DevolveCall> devolveCalls = findDevolveCalls(javaCode);
        analysis.setDevolveCalls(devolveCalls);
        
        // 3. Mapear empresta() com devolve() correspondentes
        List<ConnectionPair> pairs = mapConnectionPairs(emprestaCalls, devolveCalls, javaCode);
        analysis.setConnectionPairs(pairs);
        
        // 4. Identificar vazamentos (empresta sem devolve)
        List<ConnectionLeak> leaks = identifyLeaks(pairs, javaCode);
        analysis.setLeaks(leaks);
        
        // 5. Calcular métricas
        analysis.setTotalEmprestas(emprestaCalls.size());
        analysis.setTotalDevolves(devolveCalls.size());
        analysis.setLeakCount(leaks.size());
        analysis.setHasLeaks(!leaks.isEmpty());
        
        // 6. Determinar severity
        analysis.setSeverity(calculateSeverity(leaks));
        
        return analysis;
    }
    
    /**
     * Encontrar todas as chamadas empresta() no código
     */
    private List<EmprestaCall> findEmprestaCalls(String code) {
        List<EmprestaCall> calls = new ArrayList<>();
        String[] lines = code.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            Matcher matcher = EMPRESTA_PATTERN.matcher(line);
            
            if (matcher.find()) {
                EmprestaCall call = new EmprestaCall();
                call.setLineNumber(i + 1);
                call.setLineContent(line);
                call.setVariableName(matcher.group(1));
                call.setMethodContext(extractMethodName(code, i));
                
                // Verificar se está dentro de um try-catch
                call.setInTryBlock(isInTryBlock(code, i));
                
                calls.add(call);
            }
        }
        
        return calls;
    }
    
    /**
     * Encontrar todas as chamadas devolve() no código
     */
    private List<DevolveCall> findDevolveCalls(String code) {
        List<DevolveCall> calls = new ArrayList<>();
        String[] lines = code.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            Matcher matcher = DEVOLVE_PATTERN.matcher(line);
            
            if (matcher.find()) {
                DevolveCall call = new DevolveCall();
                call.setLineNumber(i + 1);
                call.setLineContent(line);
                call.setVariableName(matcher.group(1));
                call.setMethodContext(extractMethodName(code, i));
                
                // Verificar se está dentro de finally
                call.setInFinallyBlock(isInFinallyBlock(code, i));
                
                calls.add(call);
            }
        }
        
        return calls;
    }
    
    /**
     * Mapear empresta() com devolve() correspondentes
     */
    private List<ConnectionPair> mapConnectionPairs(List<EmprestaCall> emprestas, 
                                                   List<DevolveCall> devolves, 
                                                   String code) {
        List<ConnectionPair> pairs = new ArrayList<>();
        
        for (EmprestaCall empresta : emprestas) {
            ConnectionPair pair = new ConnectionPair();
            pair.setEmpresta(empresta);
            
            // Buscar devolve() correspondente pela variável
            DevolveCall matchingDevolve = findMatchingDevolve(empresta, devolves);
            pair.setDevolve(matchingDevolve);
            
            // Analisar estrutura try-finally
            pair.setHasTryFinally(hasTryFinallyStructure(empresta, matchingDevolve, code));
            
            pairs.add(pair);
        }
        
        return pairs;
    }
    
    /**
     * Identificar vazamentos de conexão
     */
    private List<ConnectionLeak> identifyLeaks(List<ConnectionPair> pairs, String code) {
        List<ConnectionLeak> leaks = new ArrayList<>();
        
        for (ConnectionPair pair : pairs) {
            ConnectionLeak leak = analyzeForLeak(pair, code);
            if (leak != null) {
                leaks.add(leak);
            }
        }
        
        return leaks;
    }
    
    /**
     * Analisar um par empresta/devolve para vazamentos
     */
    private ConnectionLeak analyzeForLeak(ConnectionPair pair, String code) {
        EmprestaCall empresta = pair.getEmpresta();
        DevolveCall devolve = pair.getDevolve();
        
        // Caso 1: empresta() sem devolve()
        if (devolve == null) {
            return createLeak(
                "MISSING_DEVOLVE",
                "CRITICAL",
                empresta.getLineNumber(),
                "Chamada empresta() sem devolve() correspondente",
                "Adicione devolve(" + empresta.getVariableName() + ") em bloco finally"
            );
        }
        
        // Caso 2: devolve() não está em finally
        if (!devolve.isInFinallyBlock()) {
            return createLeak(
                "DEVOLVE_NOT_IN_FINALLY", 
                "HIGH",
                devolve.getLineNumber(),
                "devolve() não está em bloco finally",
                "Mova devolve() para bloco finally para garantir execução"
            );
        }
        
        // Caso 3: return antes de devolve()
        if (hasReturnBeforeDevolve(empresta, devolve, code)) {
            return createLeak(
                "RETURN_BEFORE_DEVOLVE",
                "HIGH",
                findReturnLine(empresta, devolve, code),
                "return encontrado antes de devolve()",
                "Garanta que devolve() seja executado mesmo com return antecipado"
            );
        }
        
        // Caso 4: throw/exception antes de devolve()
        if (hasExceptionBeforeDevolve(empresta, devolve, code)) {
            return createLeak(
                "EXCEPTION_BEFORE_DEVOLVE",
                "MEDIUM",
                empresta.getLineNumber(),
                "Possível exceção antes de devolve()",
                "Use try-finally para garantir devolve() mesmo com exceções"
            );
        }
        
        return null; // Sem vazamento detectado
    }
    
    /**
     * Calcular severity baseado nos vazamentos encontrados
     */
    private String calculateSeverity(List<ConnectionLeak> leaks) {
        if (leaks.isEmpty()) return "NONE";
        
        long criticalCount = leaks.stream()
                .filter(leak -> "CRITICAL".equals(leak.getSeverity()))
                .count();
        
        if (criticalCount > 0) return "CRITICAL";
        
        long highCount = leaks.stream()
                .filter(leak -> "HIGH".equals(leak.getSeverity()))
                .count();
        
        if (highCount > 0) return "HIGH";
        
        return "MEDIUM";
    }
    
    // Métodos auxiliares
    
    private boolean isInTryBlock(String code, int lineNumber) {
        // Implementar lógica para detectar se linha está dentro de try block
        String[] lines = code.split("\\n");
        
        // Procurar try{ antes da linha atual
        for (int i = lineNumber - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.contains("try") && line.contains("{")) {
                return true;
            }
            if (line.contains("catch") || line.contains("finally")) {
                break;
            }
        }
        
        return false;
    }
    
    private boolean isInFinallyBlock(String code, int lineNumber) {
        String[] lines = code.split("\\n");
        
        // Procurar finally{ antes da linha atual
        for (int i = lineNumber - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.contains("finally") && line.contains("{")) {
                return true;
            }
            // Se encontrou outro bloco, parar
            if (line.contains("try") || line.contains("catch")) {
                break;
            }
        }
        
        return false;
    }
    
    private String extractMethodName(String code, int lineNumber) {
        String[] lines = code.split("\\n");
        
        // Procurar declaração de método antes da linha atual
        for (int i = lineNumber - 1; i >= 0; i--) {
            String line = lines[i].trim();
            
            // Pattern para métodos: public/private/protected ... nomeMétodo(
            Pattern methodPattern = Pattern.compile(
                "(public|private|protected).*\\s+(\\w+)\\s*\\("
            );
            
            Matcher matcher = methodPattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(2);
            }
        }
        
        return "unknown";
    }
    
    private DevolveCall findMatchingDevolve(EmprestaCall empresta, List<DevolveCall> devolves) {
        String targetVariable = empresta.getVariableName();
        
        // Buscar devolve que usa a mesma variável no mesmo método
        return devolves.stream()
                .filter(devolve -> targetVariable.equals(devolve.getVariableName()))
                .filter(devolve -> empresta.getMethodContext().equals(devolve.getMethodContext()))
                .filter(devolve -> devolve.getLineNumber() > empresta.getLineNumber())
                .findFirst()
                .orElse(null);
    }
}
```

### Classes de Dados para Análise

```java
/**
 * Resultado da análise de vazamentos de conexão
 */
@Data
public class ConnectionLeakAnalysis {
    private String fileName;
    private LocalDateTime analysisDate;
    private List<EmprestaCall> emprestaCalls = new ArrayList<>();
    private List<DevolveCall> devolveCalls = new ArrayList<>();
    private List<ConnectionPair> connectionPairs = new ArrayList<>();
    private List<ConnectionLeak> leaks = new ArrayList<>();
    
    private int totalEmprestas;
    private int totalDevolves;
    private int leakCount;
    private boolean hasLeaks;
    private String severity;
    
    // Métodos de conveniência
    public double getLeakPercentage() {
        if (totalEmprestas == 0) return 0.0;
        return (double) leakCount / totalEmprestas * 100;
    }
}

@Data
public class EmprestaCall {
    private int lineNumber;
    private String lineContent;
    private String variableName;
    private String methodContext;
    private boolean inTryBlock;
}

@Data
public class DevolveCall {
    private int lineNumber;
    private String lineContent;
    private String variableName;
    private String methodContext;
    private boolean inFinallyBlock;
}

@Data
public class ConnectionPair {
    private EmprestaCall empresta;
    private DevolveCall devolve;
    private boolean hasTryFinally;
}

@Data
public class ConnectionLeak {
    private String type;
    private String severity;
    private int lineNumber;
    private String description;
    private String recommendation;
    private String code;
}
```

## 📊 Algoritmo de Complexidade Ciclomática

### Implementação do Cálculo

```java
/**
 * Analisador de complexidade ciclomática
 */
@Component
public class CyclomaticComplexityAnalyzer {
    
    // Palavras-chave que aumentam complexidade
    private static final Set<String> COMPLEXITY_KEYWORDS = Set.of(
        "if", "else", "while", "for", "do", "switch", "case", 
        "catch", "&&", "||", "?", "break", "continue"
    );
    
    /**
     * Calcular complexidade ciclomática de um método
     */
    public int calculateMethodComplexity(String methodCode) {
        int complexity = 1; // Complexidade base
        
        // Remover strings e comentários para evitar falsos positivos
        String cleanCode = removeStringsAndComments(methodCode);
        
        // Contar palavras-chave de controle de fluxo
        for (String keyword : COMPLEXITY_KEYWORDS) {
            complexity += countOccurrences(cleanCode, keyword);
        }
        
        return complexity;
    }
    
    /**
     * Analisar complexidade de toda a classe
     */
    public ComplexityAnalysis analyzeClassComplexity(String javaCode, String fileName) {
        ComplexityAnalysis analysis = new ComplexityAnalysis();
        analysis.setFileName(fileName);
        
        List<MethodComplexity> methods = extractMethods(javaCode);
        analysis.setMethods(methods);
        
        // Calcular métricas da classe
        analysis.setTotalMethods(methods.size());
        analysis.setAverageComplexity(calculateAverageComplexity(methods));
        analysis.setMaxComplexity(findMaxComplexity(methods));
        analysis.setHighComplexityMethods(findHighComplexityMethods(methods));
        
        return analysis;
    }
    
    private List<MethodComplexity> extractMethods(String javaCode) {
        List<MethodComplexity> methods = new ArrayList<>();
        
        // Pattern para detectar métodos
        Pattern methodPattern = Pattern.compile(
            "(public|private|protected).*?\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{",
            Pattern.MULTILINE
        );
        
        Matcher matcher = methodPattern.matcher(javaCode);
        
        while (matcher.find()) {
            String methodName = matcher.group(2);
            int startPos = matcher.start();
            int endPos = findMethodEnd(javaCode, matcher.end());
            
            String methodCode = javaCode.substring(startPos, endPos);
            int complexity = calculateMethodComplexity(methodCode);
            
            MethodComplexity method = new MethodComplexity();
            method.setName(methodName);
            method.setComplexity(complexity);
            method.setLineStart(countLines(javaCode, startPos));
            method.setLineEnd(countLines(javaCode, endPos));
            method.setSeverity(calculateComplexitySeverity(complexity));
            
            methods.add(method);
        }
        
        return methods;
    }
    
    private String calculateComplexitySeverity(int complexity) {
        if (complexity <= 5) return "LOW";
        if (complexity <= 10) return "MEDIUM";
        if (complexity <= 15) return "HIGH";
        return "CRITICAL";
    }
}

@Data
public class ComplexityAnalysis {
    private String fileName;
    private List<MethodComplexity> methods;
    private int totalMethods;
    private double averageComplexity;
    private int maxComplexity;
    private List<MethodComplexity> highComplexityMethods;
}

@Data
public class MethodComplexity {
    private String name;
    private int complexity;
    private int lineStart;
    private int lineEnd;
    private String severity;
}
```

## 🏗️ Padrões de Arquitetura Sinqia

### Validador de Estrutura de Pacotes

```java
/**
 * Validador de padrões arquiteturais Sinqia
 */
@Component
public class SinqiaArchitectureValidator {
    
    private static final Map<String, List<String>> EXPECTED_PACKAGES = Map.of(
        "sinqia.produto.modulo", List.of("dao", "dto", "service", "controller", "util"),
        "sinqia.comum", List.of("constante", "exception", "util", "validacao")
    );
    
    /**
     * Validar estrutura de pacotes
     */
    public ArchitectureAnalysis validatePackageStructure(String javaCode, String filePath) {
        ArchitectureAnalysis analysis = new ArchitectureAnalysis();
        analysis.setFilePath(filePath);
        
        // Extrair package declaration
        String packageName = extractPackageName(javaCode);
        analysis.setPackageName(packageName);
        
        // Validar nomenclatura
        List<ArchitectureIssue> issues = new ArrayList<>();
        
        // 1. Validar estrutura de pacote
        issues.addAll(validatePackageNaming(packageName));
        
        // 2. Validar nomenclatura de classe
        issues.addAll(validateClassNaming(javaCode));
        
        // 3. Validar imports
        issues.addAll(validateImports(javaCode));
        
        // 4. Validar anotações obrigatórias
        issues.addAll(validateRequiredAnnotations(javaCode, filePath));
        
        analysis.setIssues(issues);
        analysis.setCompliant(issues.isEmpty());
        
        return analysis;
    }
    
    private List<ArchitectureIssue> validatePackageNaming(String packageName) {
        List<ArchitectureIssue> issues = new ArrayList<>();
        
        if (!packageName.startsWith("com.sinqia")) {
            issues.add(new ArchitectureIssue(
                "PACKAGE_NAMING",
                "ERROR",
                "Pacote deve começar com 'com.sinqia'",
                "Ajuste o nome do pacote para seguir padrão Sinqia"
            ));
        }
        
        // Validar estrutura hierárquica
        String[] parts = packageName.split("\\.");
        if (parts.length < 4) { // com.sinqia.produto.modulo
            issues.add(new ArchitectureIssue(
                "PACKAGE_DEPTH",
                "WARNING",
                "Estrutura de pacote muito rasa",
                "Use estrutura: com.sinqia.[produto].[modulo].[camada]"
            ));
        }
        
        return issues;
    }
    
    private List<ArchitectureIssue> validateClassNaming(String javaCode) {
        List<ArchitectureIssue> issues = new ArrayList<>();
        
        // Extrair nome da classe
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(javaCode);
        
        if (matcher.find()) {
            String className = matcher.group(1);
            
            // Validar PascalCase
            if (!Character.isUpperCase(className.charAt(0))) {
                issues.add(new ArchitectureIssue(
                    "CLASS_NAMING",
                    "ERROR",
                    "Nome da classe deve começar com maiúscula",
                    "Renomeie classe para PascalCase"
                ));
            }
            
            // Validar sufixos por tipo de classe
            if (className.contains("Service") && !className.endsWith("Service")) {
                issues.add(new ArchitectureIssue(
                    "SERVICE_NAMING",
                    "WARNING",
                    "Classes de serviço devem terminar com 'Service'",
                    "Renomeie para " + className + "Service"
                ));
            }
        }
        
        return issues;
    }
}

@Data
public class ArchitectureAnalysis {
    private String filePath;
    private String packageName;
    private List<ArchitectureIssue> issues;
    private boolean compliant;
}

@Data
@AllArgsConstructor
public class ArchitectureIssue {
    private String type;
    private String severity;
    private String description;
    private String recommendation;
}
```

## 🔒 Padrões de Segurança

### Detector de Vulnerabilidades SQL

```java
/**
 * Analisador de segurança para detectar SQL Injection
 */
@Component
public class SecurityAnalyzer {
    
    private static final Pattern SQL_CONCAT_PATTERN = Pattern.compile(
        "(SELECT|INSERT|UPDATE|DELETE).*\\+.*['\"]",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PREPARED_STATEMENT_PATTERN = Pattern.compile(
        "PreparedStatement|\\?",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Analisar vulnerabilidades de segurança
     */
    public SecurityAnalysis analyzeSecurity(String javaCode, String fileName) {
        SecurityAnalysis analysis = new SecurityAnalysis();
        analysis.setFileName(fileName);
        
        List<SecurityIssue> issues = new ArrayList<>();
        
        // 1. Detectar SQL Injection
        issues.addAll(detectSqlInjection(javaCode));
        
        // 2. Detectar tratamento inadequado de exceções
        issues.addAll(detectExceptionHandling(javaCode));
        
        // 3. Detectar logging de informações sensíveis
        issues.addAll(detectSensitiveLogging(javaCode));
        
        analysis.setIssues(issues);
        analysis.setSecurityScore(calculateSecurityScore(issues));
        
        return analysis;
    }
    
    private List<SecurityIssue> detectSqlInjection(String javaCode) {
        List<SecurityIssue> issues = new ArrayList<>();
        String[] lines = javaCode.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (SQL_CONCAT_PATTERN.matcher(line).find()) {
                // Verificar se não usa PreparedStatement
                boolean usesPreparedStatement = false;
                
                // Procurar PreparedStatement nas linhas próximas
                for (int j = Math.max(0, i-5); j < Math.min(lines.length, i+5); j++) {
                    if (PREPARED_STATEMENT_PATTERN.matcher(lines[j]).find()) {
                        usesPreparedStatement = true;
                        break;
                    }
                }
                
                if (!usesPreparedStatement) {
                    issues.add(new SecurityIssue(
                        "SQL_INJECTION",
                        "CRITICAL",
                        i + 1,
                        "Possível SQL Injection por concatenação de strings",
                        "Use PreparedStatement com parâmetros (?) em vez de concatenação"
                    ));
                }
            }
        }
        
        return issues;
    }
    
    private List<SecurityIssue> detectSensitiveLogging(String javaCode) {
        List<SecurityIssue> issues = new ArrayList<>();
        String[] lines = javaCode.split("\\n");
        
        Pattern logPattern = Pattern.compile("log\\.(debug|info|warn|error)");
        Pattern sensitivePattern = Pattern.compile("(senha|password|token|cpf|cnpj)", Pattern.CASE_INSENSITIVE);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (logPattern.matcher(line).find() && sensitivePattern.matcher(line).find()) {
                issues.add(new SecurityIssue(
                    "SENSITIVE_LOGGING",
                    "HIGH",
                    i + 1,
                    "Possível logging de informação sensível",
                    "Remova ou mascare dados sensíveis nos logs"
                ));
            }
        }
        
        return issues;
    }
}

@Data
public class SecurityAnalysis {
    private String fileName;
    private List<SecurityIssue> issues;
    private int securityScore; // 0-100
}

@Data
@AllArgsConstructor
public class SecurityIssue {
    private String type;
    private String severity;
    private int lineNumber;
    private String description;
    private String recommendation;
}
```

## 📋 Configuração dos Padrões MAYA

### application.properties

```properties
# Configurações MAYA Analysis
maya.analysis.connection-leak.enabled=true
maya.analysis.connection-leak.severity.missing-devolve=CRITICAL
maya.analysis.connection-leak.severity.not-in-finally=HIGH
maya.analysis.connection-leak.severity.return-before-devolve=HIGH

maya.analysis.complexity.enabled=true
maya.analysis.complexity.threshold.low=5
maya.analysis.complexity.threshold.medium=10
maya.analysis.complexity.threshold.high=15

maya.analysis.architecture.enabled=true
maya.analysis.architecture.enforce-sinqia-packages=true
maya.analysis.architecture.require-service-suffix=true

maya.analysis.security.enabled=true
maya.analysis.security.sql-injection.enabled=true
maya.analysis.security.sensitive-logging.enabled=true
```

### Configuração de Regras Customizáveis

```java
@Configuration
@ConfigurationProperties("maya.rules")
@Data
public class MayaRulesConfiguration {
    
    private ConnectionLeakRules connectionLeak = new ConnectionLeakRules();
    private ComplexityRules complexity = new ComplexityRules();
    private ArchitectureRules architecture = new ArchitectureRules();
    private SecurityRules security = new SecurityRules();
    
    @Data
    public static class ConnectionLeakRules {
        private boolean enabled = true;
        private List<String> empresaPatterns = List.of("empresta", "obterConexao");
        private List<String> devolvePatterns = List.of("devolve", "liberarConexao");
        private boolean requireFinally = true;
        private String severity = "CRITICAL";
    }
    
    @Data
    public static class ComplexityRules {
        private boolean enabled = true;
        private int lowThreshold = 5;
        private int mediumThreshold = 10;
        private int highThreshold = 15;
        private int criticalThreshold = 20;
    }
    
    @Data
    public static class ArchitectureRules {
        private boolean enabled = true;
        private String requiredPackagePrefix = "com.sinqia";
        private Map<String, String> classSuffixes = Map.of(
            "Service", "Service",
            "Controller", "Controller",
            "Repository", "Repository"
        );
    }
}
```

## 🧪 Testes dos Algoritmos MAYA

### Casos de Teste para Connection Leak

```java
@SpringBootTest
class MayaAnalysisServiceTest {
    
    @Autowired
    private MayaAnalysisService mayaService;
    
    @Test
    void testConnectionLeakDetection() {
        String codeWithLeak = """
            public void exemploComVazamento() {
                Connection conn = ConexaoFactory.empresta();
                
                if (condicao) {
                    return; // PROBLEMA: return sem devolve
                }
                
                // fazendo algo com conexão
                conn.execute("SELECT * FROM tabela");
            }
            """;
        
        ConnectionLeakAnalysis analysis = mayaService.analyzeConnectionLeaks(codeWithLeak, "Test.java");
        
        assertTrue(analysis.isHasLeaks());
        assertEquals(1, analysis.getLeakCount());
        assertEquals("CRITICAL", analysis.getSeverity());
        
        ConnectionLeak leak = analysis.getLeaks().get(0);
        assertEquals("RETURN_BEFORE_DEVOLVE", leak.getType());
    }
    
    @Test
    void testCorrectConnectionUsage() {
        String correctCode = """
            public void exemploCorreto() {
                Connection conn = null;
                try {
                    conn = ConexaoFactory.empresta();
                    conn.execute("SELECT * FROM tabela");
                } finally {
                    if (conn != null) {
                        ConexaoFactory.devolve(conn);
                    }
                }
            }
            """;
        
        ConnectionLeakAnalysis analysis = mayaService.analyzeConnectionLeaks(correctCode, "Test.java");
        
        assertFalse(analysis.isHasLeaks());
        assertEquals(0, analysis.getLeakCount());
        assertEquals("NONE", analysis.getSeverity());
    }
}
```

## 📈 Métricas e Relatórios

### Gerador de Métricas MAYA

```java
@Service
public class MayaMetricsService {
    
    /**
     * Gerar métricas consolidadas de análise
     */
    public MayaMetrics generateMetrics(List<FileAnalysis> analyses) {
        MayaMetrics metrics = new MayaMetrics();
        
        // Métricas de Connection Leak
        long totalFiles = analyses.size();
        long filesWithLeaks = analyses.stream()
                .filter(a -> a.getConnectionLeakAnalysis() != null)
                .filter(a -> a.getConnectionLeakAnalysis().isHasLeaks())
                .count();
        
        metrics.setTotalFiles((int) totalFiles);
        metrics.setFilesWithConnectionLeaks((int) filesWithLeaks);
        metrics.setConnectionLeakPercentage((double) filesWithLeaks / totalFiles * 100);
        
        // Métricas de Complexidade
        double avgComplexity = analyses.stream()
                .filter(a -> a.getComplexityAnalysis() != null)
                .mapToDouble(a -> a.getComplexityAnalysis().getAverageComplexity())
                .average()
                .orElse(0.0);
        
        metrics.setAverageComplexity(avgComplexity);
        
        // Top issues
        Map<String, Long> issueTypes = analyses.stream()
                .flatMap(a -> a.getAnalysisIssues().stream())
                .collect(Collectors.groupingBy(
                    AnalysisIssue::getType,
                    Collectors.counting()
                ));
        
        metrics.setTopIssueTypes(issueTypes);
        
        return metrics;
    }
}

@Data
public class MayaMetrics {
    private int totalFiles;
    private int filesWithConnectionLeaks;
    private double connectionLeakPercentage;
    private double averageComplexity;
    private Map<String, Long> topIssueTypes;
    private LocalDateTime generatedAt = LocalDateTime.now();
}
```

## 🎯 Próximos Passos

1. Implemente os algoritmos principais seguindo este guia
2. Configure as regras customizáveis via properties
3. Desenvolva testes unitários abrangentes
4. Integre com o sistema de relatórios
5. Ajuste os padrões conforme feedback dos usuários

Os padrões MAYA são o diferencial do sistema, focando nos problemas reais do código Sinqia com algoritmos específicos e eficazes.
