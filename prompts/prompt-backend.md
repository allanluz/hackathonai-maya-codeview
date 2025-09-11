# MAYA Code Review System - Backend (Spring Boot)

## 📋 Visão Geral do Backend

O backend é desenvolvido em **Spring Boot 3.2.0** com arquitetura RESTful, implementando os algoritmos específicos de análise MAYA e integração com sistemas externos (TFS/Azure DevOps e Sinqia AI).

## 🏗️ Estrutura do Projeto

```
backend/
├── pom.xml                          # Configuração Maven
├── src/main/java/com/sinqia/maya/
│   ├── MayaCodeReviewApplication.java   # Classe principal
│   ├── config/
│   │   ├── DataInitializer.java         # Dados iniciais
│   │   └── WebConfig.java               # Configuração CORS
│   ├── controller/
│   │   ├── AiController.java            # API de IA
│   │   ├── CodeReviewController.java    # API principal
│   │   ├── CommitImportController.java  # Importação commits
│   │   ├── ConfigurationController.java # Configurações
│   │   └── TfsController.java           # Integração TFS
│   ├── dto/
│   │   ├── AiApiDto.java                # DTOs da API de IA
│   │   ├── CodeAnalysisRequest.java     # Requisição análise
│   │   ├── CodeAnalysisResponse.java    # Resposta análise
│   │   ├── ConfigurationSettingsDto.java # DTO configuração
│   │   ├── LLMModel.java                # Modelo LLM
│   │   └── TfsCommitDto.java            # DTO commit TFS
│   ├── entity/
│   │   ├── AnalysisIssue.java           # Issue de análise
│   │   ├── AuxiliaryFile.java           # Arquivo auxiliar
│   │   ├── CodeReview.java              # Revisão principal
│   │   ├── ConfigurationSettings.java  # Configurações
│   │   └── FileAnalysis.java            # Análise de arquivo
│   ├── repository/
│   │   ├── AnalysisIssueRepository.java # Repo issues
│   │   ├── AuxiliaryFileRepository.java # Repo arquivos
│   │   ├── CodeReviewRepository.java    # Repo reviews
│   │   ├── ConfigurationSettingsRepository.java # Repo config
│   │   └── FileAnalysisRepository.java  # Repo análises
│   └── service/
│       ├── CodeReviewAnalysisService.java   # Análise de review
│       ├── ConfigurationService.java       # Serviço config
│       ├── MayaAnalysisService.java        # Análise MAYA
│       ├── SinqiaAiService.java           # Integração IA
│       ├── TfsIntegrationService.java     # Integração TFS
│       └── TfsService.java                # Serviço TFS
└── src/main/resources/
    ├── application.properties           # Configuração principal
    ├── application-dev.properties       # Config desenvolvimento
    ├── application-prod.properties      # Config produção
    └── data.sql                        # Dados iniciais
```

## 🛠️ Configuração Maven (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.sinqia</groupId>
    <artifactId>maya-code-review</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

## 🗄️ Entidades JPA

### CodeReview (Entidade Principal)

```java
@Entity
@Table(name = "code_reviews")
public class CodeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pull_request_id")
    private String pullRequestId;
    
    @Column(name = "commit_sha", nullable = false)
    private String commitSha;
    
    @Column(name = "repository_name", nullable = false)
    private String repositoryName;
    
    @Column(name = "project_name", nullable = false)
    private String projectName;
    
    @Column(name = "author", nullable = false)
    private String author;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @Column(name = "critical_issues")
    private Integer criticalIssues = 0;
    
    @Column(name = "total_issues")
    private Integer totalIssues = 0;
    
    @Column(name = "analysis_score")
    private Double analysisScore = 0.0;
    
    @Column(name = "llm_model")
    private String llmModel;
    
    @Column(name = "analysis_options", columnDefinition = "TEXT")
    private String analysisOptionsJson;
    
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "codeReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FileAnalysis> fileAnalyses = new ArrayList<>();
    
    public enum ReviewStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }
    
    // Constructors, getters, setters...
}
```

### FileAnalysis (Análise por Arquivo)

```java
@Entity
@Table(name = "file_analyses")
public class FileAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_review_id", nullable = false)
    private CodeReview codeReview;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "class_name")
    private String className;
    
    @Column(name = "language")
    private String language;
    
    @Column(name = "line_count")
    private Integer lineCount = 0;
    
    @Column(name = "complexity_score")
    private Double complexityScore = 0.0;
    
    @Column(name = "connection_imbalance")
    private Double connectionImbalance = 0.0;
    
    @Column(name = "score")
    private Double score = 0.0;
    
    // Campos específicos MAYA
    @Column(name = "connection_empresta")
    private Integer connectionEmpresta = 0;
    
    @Column(name = "connection_devolve")
    private Integer connectionDevolve = 0;
    
    @Column(name = "connection_balanced")
    private Boolean connectionBalanced = true;
    
    @Column(name = "has_type_changes")
    private Boolean hasTypeChanges = false;
    
    @Column(name = "has_method_changes")
    private Boolean hasMethodChanges = false;
    
    @Column(name = "has_validation_changes")
    private Boolean hasValidationChanges = false;
    
    @Column(name = "analysis_report", columnDefinition = "TEXT")
    private String analysisReport;
    
    @Column(name = "markdown_report", columnDefinition = "TEXT")
    private String markdownReport;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "fileAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AnalysisIssue> issues = new ArrayList<>();
    
    // Constructors, getters, setters, helper methods...
}
```

### AnalysisIssue (Issues Detectados)

```java
@Entity
@Table(name = "analysis_issues")
public class AnalysisIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_analysis_id", nullable = false)
    private FileAnalysis fileAnalysis;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IssueSeverity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private IssueType type;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "line_number")
    private Integer lineNumber;
    
    @Column(name = "column_number")
    private Integer columnNumber;
    
    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum IssueSeverity {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    public enum IssueType {
        CONNECTION_LEAK, CODE_QUALITY, SECURITY_ISSUE, 
        PERFORMANCE_ISSUE, STYLE_VIOLATION, TYPE_CHANGE,
        METHOD_CHANGE, VALIDATION_CHANGE, COMPLEXITY
    }
    
    // Constructors, getters, setters...
}
```

## 🔧 Serviços Principais

### MayaAnalysisService (Core do Sistema)

```java
@Service
public class MayaAnalysisService {
    
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("class\\s+(\\w+)");
    private static final Pattern EMPRESTA_PATTERN = Pattern.compile("empresta\\(");
    private static final Pattern DEVOLVE_PATTERN = Pattern.compile("devolve\\(");
    
    @Autowired
    private CodeReviewRepository codeReviewRepository;
    
    @Autowired
    private FileAnalysisRepository fileAnalysisRepository;
    
    @Autowired
    private TfsService tfsService;
    
    @Autowired
    private SinqiaAiService sinqiaAiService;
    
    /**
     * Análise principal de um commit
     */
    @Transactional
    public CodeReview analyzeCommit(String commitSha, String repositoryName, 
                                   String projectName, String llmModel, 
                                   String analysisOptionsJson) {
        
        CodeReview review = createCodeReview(commitSha, repositoryName, 
                                           projectName, llmModel, analysisOptionsJson);
        
        // Buscar mudanças do commit
        TfsService.CommitChanges changes = tfsService.getCommitChanges(
            projectName, repositoryName, commitSha);
        
        List<String> javaFiles = extractChangedJavaFiles(changes);
        
        for (String filePath : javaFiles) {
            FileAnalysis analysis = analyzeFile(review.getId(), filePath, 
                                              commitSha, projectName, repositoryName);
            review.getFileAnalyses().add(analysis);
        }
        
        // Calcular métricas finais
        updateReviewMetrics(review);
        
        return codeReviewRepository.save(review);
    }
    
    /**
     * Análise específica MAYA de um arquivo
     */
    private FileAnalysis analyzeFile(Long reviewId, String filePath, String commitSha,
                                   String projectName, String repositoryName) {
        
        String content = tfsService.getFileContent(projectName, repositoryName, 
                                                 commitSha, filePath);
        
        FileAnalysis analysis = new FileAnalysis();
        analysis.setCodeReview(codeReviewRepository.findById(reviewId).orElse(null));
        analysis.setFilePath(filePath);
        analysis.setClassName(extractClassName(content));
        analysis.setLanguage(detectLanguage(filePath));
        
        // Análises específicas MAYA
        analyzeConnections(analysis, content);
        analyzeComplexity(analysis, content);
        analyzeTypeChanges(analysis, content);
        analyzeMethodChanges(analysis, content);
        analyzeValidationChanges(analysis, content);
        
        // Gerar relatório
        analysis.setMarkdownReport(generateMarkdownReport(analysis, content));
        
        // Criar issues baseados na análise
        createIssuesFromAnalysis(analysis);
        
        // Calcular score final
        analysis.setScore(calculateFileScore(analysis));
        
        return fileAnalysisRepository.save(analysis);
    }
    
    /**
     * Análise de conexões empresta/devolve (core MAYA)
     */
    private void analyzeConnections(FileAnalysis analysis, String content) {
        if (content == null) return;
        
        int empresta = countOccurrences(content, EMPRESTA_PATTERN);
        int devolve = countOccurrences(content, DEVOLVE_PATTERN);
        
        analysis.setConnectionEmpresta(empresta);
        analysis.setConnectionDevolve(devolve);
        analysis.setConnectionBalanced(empresta == devolve);
        
        // Calcular desequilíbrio
        if (empresta + devolve > 0) {
            double imbalance = Math.abs(empresta - devolve) / (double)(empresta + devolve) * 100;
            analysis.setConnectionImbalance(imbalance);
        }
    }
    
    /**
     * Análise de complexidade ciclomática
     */
    private void analyzeComplexity(FileAnalysis analysis, String content) {
        if (content == null) return;
        
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
        analysis.setLineCount(content.split("\\n").length);
    }
    
    /**
     * Criar issues baseados na análise
     */
    private void createIssuesFromAnalysis(FileAnalysis analysis) {
        List<AnalysisIssue> issues = new ArrayList<>();
        
        // Issue crítico: conexões desbalanceadas
        if (!analysis.getConnectionBalanced()) {
            int difference = Math.abs(analysis.getConnectionEmpresta() - 
                                    analysis.getConnectionDevolve());
            
            AnalysisIssue issue = new AnalysisIssue();
            issue.setFileAnalysis(analysis);
            issue.setType(AnalysisIssue.IssueType.CONNECTION_LEAK);
            issue.setSeverity(difference > 5 ? AnalysisIssue.IssueSeverity.CRITICAL : 
                            AnalysisIssue.IssueSeverity.WARNING);
            issue.setTitle("Unbalanced Connections");
            issue.setDescription(String.format(
                "Unbalanced connections detected: %d empresta vs %d devolve (difference: %d). " +
                "This may cause connection leaks.",
                analysis.getConnectionEmpresta(), analysis.getConnectionDevolve(), difference));
            issue.setSuggestion("Ensure every empresta() call has a corresponding devolve() call, " +
                              "preferably in a finally block.");
            issues.add(issue);
        }
        
        // Issue: alta complexidade
        if (analysis.getComplexityScore() > 10) {
            AnalysisIssue issue = new AnalysisIssue();
            issue.setFileAnalysis(analysis);
            issue.setType(AnalysisIssue.IssueType.COMPLEXITY);
            issue.setSeverity(AnalysisIssue.IssueSeverity.WARNING);
            issue.setTitle("High Cyclomatic Complexity");
            issue.setDescription(String.format(
                "High cyclomatic complexity: %.0f (threshold: 10). " +
                "Consider refactoring to reduce complexity.",
                analysis.getComplexityScore()));
            issue.setSuggestion("Break down complex methods into smaller, more focused methods.");
            issues.add(issue);
        }
        
        analysis.setIssues(issues);
    }
    
    /**
     * Calcular score de qualidade do arquivo
     */
    private double calculateFileScore(FileAnalysis analysis) {
        double score = 100.0;
        
        // Penalizar conexões desbalanceadas
        if (!analysis.getConnectionBalanced()) {
            int difference = Math.abs(analysis.getConnectionEmpresta() - 
                                    analysis.getConnectionDevolve());
            score -= Math.min(50.0, difference * 10.0);
        }
        
        // Penalizar alta complexidade
        if (analysis.getComplexityScore() > 10) {
            score -= Math.min(30.0, (analysis.getComplexityScore() - 10) * 2.0);
        }
        
        // Penalizar issues críticos
        long criticalIssues = analysis.getIssues().stream()
            .filter(issue -> issue.getSeverity() == AnalysisIssue.IssueSeverity.CRITICAL)
            .count();
        score -= criticalIssues * 20.0;
        
        return Math.max(0.0, score);
    }
    
    private int countOccurrences(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    // Métodos auxiliares: extractClassName, generateMarkdownReport, etc.
}
```

## 🔌 Controllers REST

### CodeReviewController (API Principal)

```java
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
@Tag(name = "Code Reviews", description = "API para gerenciamento de revisões de código")
public class CodeReviewController {
    
    @Autowired
    private MayaAnalysisService mayaAnalysisService;
    
    @Autowired
    private CodeReviewRepository codeReviewRepository;
    
    /**
     * Listar todas as revisões
     */
    @GetMapping
    @Operation(summary = "Listar revisões", description = "Lista todas as revisões com paginação")
    public ResponseEntity<Page<CodeReview>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String repository,
            @RequestParam(required = false) CodeReview.ReviewStatus status) {
        
        Specification<CodeReview> spec = Specification.where(null);
        
        if (author != null && !author.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
        }
        
        if (repository != null && !repository.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("repositoryName")), "%" + repository.toLowerCase() + "%"));
        }
        
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CodeReview> reviews = codeReviewRepository.findAll(spec, pageable);
        
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Criar revisão a partir de commit TFS
     */
    @PostMapping("/create-from-commit")
    @Operation(summary = "Criar revisão", description = "Cria uma nova revisão a partir de commit")
    public ResponseEntity<Map<String, Object>> createFromCommit(
            @Valid @RequestBody CreateReviewRequest request) {
        
        try {
            CodeReview review = mayaAnalysisService.analyzeCommit(
                request.getCommitSha(),
                request.getRepositoryName(),
                request.getProjectName(),
                request.getLlmModel(),
                request.getAnalysisOptionsJson()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reviewId", review.getId());
            response.put("message", "Code review created successfully");
            response.put("review", review);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating code review", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating code review: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obter detalhes de uma revisão
     */
    @GetMapping("/{id}")
    @Operation(summary = "Detalhes da revisão", description = "Obtém detalhes completos de uma revisão")
    public ResponseEntity<CodeReview> getReviewById(@PathVariable Long id) {
        Optional<CodeReview> review = codeReviewRepository.findById(id);
        return review.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obter métricas do dashboard
     */
    @GetMapping("/metrics")
    @Operation(summary = "Métricas", description = "Obtém métricas para o dashboard")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Total de revisões
        long totalReviews = codeReviewRepository.count();
        metrics.put("totalReviews", totalReviews);
        
        // Revisões por status
        metrics.put("pendingReviews", 
            codeReviewRepository.countByStatus(CodeReview.ReviewStatus.PENDING));
        metrics.put("completedReviews", 
            codeReviewRepository.countByStatus(CodeReview.ReviewStatus.COMPLETED));
        metrics.put("failedReviews", 
            codeReviewRepository.countByStatus(CodeReview.ReviewStatus.FAILED));
        
        // Issues críticos
        metrics.put("totalCriticalIssues", 
            codeReviewRepository.sumCriticalIssues());
        
        // Score médio
        Double avgScore = codeReviewRepository.averageAnalysisScore();
        metrics.put("averageScore", avgScore != null ? avgScore : 0.0);
        
        return ResponseEntity.ok(metrics);
    }
    
    // DTOs para requests
    @Data
    public static class CreateReviewRequest {
        @NotBlank
        private String commitSha;
        
        @NotBlank
        private String repositoryName;
        
        @NotBlank
        private String projectName;
        
        private String llmModel;
        private String analysisOptionsJson;
    }
}
```

## 📋 Configurações

### application.properties

```properties
# Application Configuration
spring.application.name=maya-code-review

# Server Configuration
server.port=8081

# Database Configuration (H2 for development)
spring.datasource.url=jdbc:h2:mem:maya_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=password
spring.datasource.driver-class-name=org.h2.Driver

# H2 Console (for testing)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate Configuration
spring.jpa.database=h2
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.defer-datasource-initialization=true

# Connection Pool Configuration
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=America/Sao_Paulo

# TFS Configuration
tfs.server.url=${TFS_SERVER_URL:https://tfs.sinqia.com.br}
tfs.collection=${TFS_COLLECTION:GestaoRecursos}
tfs.username=${TFS_USERNAME:allan.luz}
tfs.password=${TFS_PASSWORD:}

# Sinqia AI Configuration
maya.ai.base-url=${SINQIA_AI_URL:http://everai.sinqia.com.br}
maya.ai.timeout=30000
maya.ai.enabled=true

# CORS Configuration
maya.cors.allowed-origins=http://localhost:4200,http://localhost:3000

# Logging Configuration
logging.level.com.sinqia.maya=INFO
logging.level.org.springframework.web=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# SpringDoc OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

## 🚀 Execução

### Comandos Maven

```bash
# Compilar o projeto
mvn clean compile

# Executar testes
mvn test

# Gerar JAR
mvn clean package

# Executar a aplicação
mvn spring-boot:run

# Executar com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Perfis de Ambiente

- **dev**: H2 em memória, logs detalhados
- **prod**: SQL Server, logs otimizados

### APIs Disponíveis

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **H2 Console**: http://localhost:8081/h2-console
- **Actuator**: http://localhost:8081/actuator/health
- **API Docs**: http://localhost:8081/v3/api-docs

## 🔧 Próximos Passos

1. Configure o ambiente seguindo `prompt-configuracao.md`
2. Implemente as entidades JPA
3. Desenvolva os serviços de análise MAYA
4. Crie os controllers REST
5. Integre com TFS seguindo `prompt-integracoes.md`
6. Teste todas as funcionalidades

O backend está preparado para ser o núcleo robusto do sistema MAYA, implementando todos os algoritmos específicos de análise de código da Sinqia.
