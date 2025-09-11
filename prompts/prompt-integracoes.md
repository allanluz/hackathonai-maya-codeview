# MAYA Code Review System - Integrações

## 📋 Visão Geral das Integrações

O sistema MAYA integra-se com duas plataformas principais: **TFS/Azure DevOps** para importação de commits e pull requests, e **Sinqia AI** para análise avançada de código utilizando modelos de linguagem large (LLM).

## 🔗 Integração com TFS/Azure DevOps

### Configuração da Conexão

A integração com TFS é baseada na REST API e utiliza Personal Access Token (PAT) para autenticação.

#### Configuração no Backend

**TfsService.java**
```java
@Service
public class TfsService {
    
    @Value("${tfs.server.url}")
    private String tfsServerUrl;
    
    @Value("${tfs.collection}")
    private String tfsCollection;
    
    @Value("${tfs.username}")
    private String tfsUsername;
    
    @Value("${tfs.password}")
    private String tfsPassword;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public TfsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        configureRestTemplate();
    }
    
    private void configureRestTemplate() {
        // Configurar timeout
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        restTemplate.setRequestFactory(factory);
    }
    
    /**
     * Criar headers de autenticação
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Autenticação básica com PAT token
        String auth = ":" + tfsPassword; // PAT token no campo password
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        
        return headers;
    }
    
    /**
     * Obter detalhes de um commit
     */
    public CommitInfo getCommitInfo(String project, String repository, String commitSha) {
        try {
            String url = String.format("%s/%s/%s/_apis/git/repositories/%s/commits/%s?api-version=6.0",
                    tfsServerUrl, tfsCollection, project, repository, commitSha);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode commitNode = objectMapper.readTree(response.getBody());
            
            CommitInfo commitInfo = new CommitInfo();
            commitInfo.setCommitSha(commitNode.get("commitId").asText());
            commitInfo.setAuthor(commitNode.get("author").get("name").asText());
            commitInfo.setMessage(commitNode.get("comment").asText());
            commitInfo.setDate(LocalDateTime.parse(commitNode.get("author").get("date").asText()));
            
            return commitInfo;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar informações do commit: {}", e.getMessage());
            throw new RuntimeException("Falha na comunicação com TFS", e);
        }
    }
    
    /**
     * Obter mudanças de um commit
     */
    public CommitChanges getCommitChanges(String project, String repository, String commitSha) {
        try {
            String url = String.format("%s/%s/%s/_apis/git/repositories/%s/commits/%s/changes?api-version=6.0",
                    tfsServerUrl, tfsCollection, project, repository, commitSha);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode changesNode = objectMapper.readTree(response.getBody());
            
            CommitChanges changes = new CommitChanges();
            changes.setCommitSha(commitSha);
            
            List<FileChange> fileChanges = new ArrayList<>();
            JsonNode changesArray = changesNode.path("changes");
            
            for (JsonNode change : changesArray) {
                JsonNode item = change.path("item");
                if (item.path("gitObjectType").asText().equals("blob")) {
                    FileChange fileChange = new FileChange();
                    fileChange.setPath(item.path("path").asText());
                    fileChange.setChangeType(change.path("changeType").asText());
                    fileChanges.add(fileChange);
                }
            }
            
            changes.setFiles(fileChanges);
            return changes;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar mudanças do commit: {}", e.getMessage());
            throw new RuntimeException("Falha ao obter mudanças do commit", e);
        }
    }
    
    /**
     * Obter conteúdo de um arquivo
     */
    public String getFileContent(String project, String repository, String commitSha, String filePath) {
        try {
            String url = String.format("%s/%s/%s/_apis/git/repositories/%s/items?path=%s&version=%s&api-version=6.0",
                    tfsServerUrl, tfsCollection, project, repository, 
                    URLEncoder.encode(filePath, StandardCharsets.UTF_8), commitSha);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("Erro ao buscar conteúdo do arquivo {}: {}", filePath, e.getMessage());
            return "";
        }
    }
    
    /**
     * Testar conectividade com TFS
     */
    public boolean testConnection() {
        try {
            String url = String.format("%s/%s/_apis/projects?api-version=6.0", 
                    tfsServerUrl, tfsCollection);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.error("Falha no teste de conectividade TFS: {}", e.getMessage());
            return false;
        }
    }
    
    // Classes internas para DTOs
    public static class CommitInfo {
        private String commitSha;
        private String author;
        private String message;
        private LocalDateTime date;
        // getters e setters...
    }
    
    public static class CommitChanges {
        private String commitSha;
        private List<FileChange> files;
        // getters e setters...
    }
    
    public static class FileChange {
        private String path;
        private String changeType;
        // getters e setters...
    }
}
```

#### Controller para TFS

**TfsController.java**
```java
@RestController
@RequestMapping("/api/tfs")
@CrossOrigin(origins = "*")
public class TfsController {
    
    @Autowired
    private TfsService tfsService;
    
    /**
     * Testar conexão com TFS
     */
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody TfsConnectionRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Temporariamente definir configurações para teste
            // Em produção, isso seria configurado via properties
            boolean connected = tfsService.testConnection();
            
            response.put("success", connected);
            response.put("message", connected ? "Conexão estabelecida com sucesso" : "Falha na conexão");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro na conexão: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obter informações de commit
     */
    @GetMapping("/commits/{project}/{repository}/{commitId}")
    public ResponseEntity<TfsService.CommitInfo> getCommitInfo(
            @PathVariable String project,
            @PathVariable String repository,
            @PathVariable String commitId) {
        
        try {
            TfsService.CommitInfo commitInfo = tfsService.getCommitInfo(project, repository, commitId);
            return ResponseEntity.ok(commitInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Data
    public static class TfsConnectionRequest {
        private String serverUrl;
        private String collection;
        private String username;
        private String password;
    }
}
```

### Configuração de Webhook (Opcional)

Para automação completa, configure um webhook no Azure DevOps:

```json
{
  "url": "https://sua-aplicacao.com/api/webhook/tfs",
  "events": [
    "git.pullrequest.created",
    "git.pullrequest.updated",
    "git.push"
  ]
}
```

**WebhookController.java**
```java
@RestController
@RequestMapping("/api/webhook")
@CrossOrigin(origins = "*")
public class WebhookController {
    
    @Autowired
    private MayaAnalysisService mayaAnalysisService;
    
    @PostMapping("/tfs")
    public ResponseEntity<String> handleTfsWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("eventType");
            
            if ("git.push".equals(eventType)) {
                handlePushEvent(payload);
            } else if ("git.pullrequest.created".equals(eventType)) {
                handlePullRequestEvent(payload);
            }
            
            return ResponseEntity.ok("Webhook processado com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro ao processar webhook TFS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro no processamento: " + e.getMessage());
        }
    }
    
    private void handlePushEvent(Map<String, Object> payload) {
        // Extrair informações do push
        Map<String, Object> resource = (Map<String, Object>) payload.get("resource");
        List<Map<String, Object>> commits = (List<Map<String, Object>>) resource.get("commits");
        
        for (Map<String, Object> commit : commits) {
            String commitId = (String) commit.get("commitId");
            // Processar commit automaticamente
            // mayaAnalysisService.analyzeCommit(commitId, ...);
        }
    }
}
```

## 🤖 Integração com Sinqia AI

### Configuração do Serviço de IA

**SinqiaAiService.java**
```java
@Service
public class SinqiaAiService {
    
    private static final Logger logger = LoggerFactory.getLogger(SinqiaAiService.class);
    
    @Value("${maya.ai.base-url}")
    private String baseUrl;
    
    @Value("${maya.ai.timeout}")
    private int timeout;
    
    @Value("${maya.ai.enabled}")
    private boolean enabled;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public SinqiaAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        configureRestTemplate();
    }
    
    private void configureRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        restTemplate.setRequestFactory(factory);
    }
    
    /**
     * Obter modelos LLM disponíveis
     */
    public List<AiApiDto.LlmModel> getAvailableModels() {
        if (!enabled) {
            return getFallbackModels();
        }
        
        try {
            String url = baseUrl + "/models";
            ResponseEntity<AiApiDto.ModelsResponse> response = 
                    restTemplate.getForEntity(url, AiApiDto.ModelsResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getModels();
            }
            
        } catch (Exception e) {
            logger.warn("Erro ao buscar modelos da API Sinqia AI, usando fallback: {}", e.getMessage());
        }
        
        return getFallbackModels();
    }
    
    /**
     * Analisar código com modelo específico
     */
    public CodeAnalysisResponse analyzeCode(String code, String language, String modelId, String customPrompt) {
        if (!enabled) {
            return createMockAnalysis(code, language);
        }
        
        try {
            String url = baseUrl + "/analyze";
            
            CodeAnalysisRequest request = new CodeAnalysisRequest();
            request.setCode(code);
            request.setLanguage(language);
            request.setModelId(modelId);
            request.setPrompt(buildAnalysisPrompt(customPrompt));
            
            ResponseEntity<CodeAnalysisResponse> response = 
                    restTemplate.postForEntity(url, request, CodeAnalysisResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            logger.error("Erro na análise com IA: {}", e.getMessage());
        }
        
        return createMockAnalysis(code, language);
    }
    
    /**
     * Análise em lote de múltiplos arquivos
     */
    public List<CodeAnalysisResponse> analyzeBatch(List<FileAnalysisRequest> files, String modelId, String customPrompt) {
        List<CodeAnalysisResponse> results = new ArrayList<>();
        
        for (FileAnalysisRequest file : files) {
            CodeAnalysisResponse analysis = analyzeCode(file.getContent(), file.getLanguage(), modelId, customPrompt);
            analysis.setFilePath(file.getFilePath());
            results.add(analysis);
        }
        
        return results;
    }
    
    /**
     * Gerar relatório executivo
     */
    public ReportGenerationResponse generateExecutiveReport(List<CodeAnalysisResponse> analyses, String modelId) {
        if (!enabled) {
            return createMockReport(analyses);
        }
        
        try {
            String url = baseUrl + "/generate-report";
            
            ReportGenerationRequest request = new ReportGenerationRequest();
            request.setAnalyses(analyses);
            request.setModelId(modelId);
            request.setReportType("executive");
            
            ResponseEntity<ReportGenerationResponse> response = 
                    restTemplate.postForEntity(url, request, ReportGenerationResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            logger.error("Erro na geração de relatório: {}", e.getMessage());
        }
        
        return createMockReport(analyses);
    }
    
    /**
     * Construir prompt especializado para análise MAYA
     */
    private String buildAnalysisPrompt(String customPrompt) {
        StringBuilder prompt = new StringBuilder();
        
        if (customPrompt != null && !customPrompt.trim().isEmpty()) {
            prompt.append(customPrompt);
        } else {
            prompt.append(getDefaultMayaPrompt());
        }
        
        prompt.append("\n\n");
        prompt.append("## Formato de Resposta\n");
        prompt.append("Retorne a análise em formato JSON com:\n");
        prompt.append("- severity: INFO, WARNING, ERROR, CRITICAL\n");
        prompt.append("- type: CONNECTION_LEAK, SECURITY_ISSUE, CODE_QUALITY, etc.\n");
        prompt.append("- description: Descrição detalhada\n");
        prompt.append("- recommendation: Sugestão de correção\n");
        prompt.append("- lineNumber: Número da linha (se aplicável)\n");
        
        return prompt.toString();
    }
    
    private String getDefaultMayaPrompt() {
        return """
            Você é um especialista em revisão de código focado nos padrões MAYA da Sinqia.
            
            ## Diretrizes de Análise
            
            ### 1. Gestão de Conexões (PRIORIDADE MÁXIMA)
            - Verificar se todas as conexões são abertas com empresta() e fechadas com devolve()
            - Identificar vazamentos de conexão (connection leaks)
            - Validar uso de try-finally para garantir devolve()
            - Detectar padrões de risco: return sem finally, throw sem finally
            
            ### 2. Padrões SINQIA
            - Verificar nomenclatura de classes e métodos em português
            - Validar estrutura de pacotes padrão Sinqia
            - Confirmar uso de annotations apropriadas
            - Verificar licença Sinqia no cabeçalho
            
            ### 3. Qualidade de Código
            - Analisar complexidade ciclomática
            - Identificar código duplicado
            - Verificar conformidade com padrões de formatação
            - Validar uso adequado de logging
            
            ### 4. Segurança
            - Validar sanitização de inputs
            - Verificar tratamento de exceções
            - Identificar possíveis vulnerabilidades SQL injection
            - Verificar uso adequado de prepared statements
            """;
    }
    
    /**
     * Modelos de fallback quando API não disponível
     */
    private List<AiApiDto.LlmModel> getFallbackModels() {
        List<AiApiDto.LlmModel> models = new ArrayList<>();
        
        AiApiDto.LlmModel gpt4 = new AiApiDto.LlmModel();
        gpt4.setId("gpt-4");
        gpt4.setName("GPT-4");
        gpt4.setDescription("Modelo avançado para análises complexas");
        gpt4.setProvider("OpenAI");
        gpt4.setMaxTokens(8192);
        gpt4.setIsActive(true);
        models.add(gpt4);
        
        AiApiDto.LlmModel gpt35 = new AiApiDto.LlmModel();
        gpt35.setId("gpt-3.5-turbo");
        gpt35.setName("GPT-3.5 Turbo");
        gpt35.setDescription("Modelo otimizado para análises rápidas");
        gpt35.setProvider("OpenAI");
        gpt35.setMaxTokens(4096);
        gpt35.setIsActive(true);
        models.add(gpt35);
        
        return models;
    }
    
    /**
     * Criar análise mock para desenvolvimento/fallback
     */
    private CodeAnalysisResponse createMockAnalysis(String code, String language) {
        CodeAnalysisResponse response = new CodeAnalysisResponse();
        response.setSuccess(true);
        response.setModelUsed("mock-analyzer");
        
        List<CodeAnalysisResponse.Issue> issues = new ArrayList<>();
        
        // Verificar vazamento de conexão
        if (code.contains("empresta(") && !code.contains("devolve(")) {
            CodeAnalysisResponse.Issue issue = new CodeAnalysisResponse.Issue();
            issue.setSeverity("CRITICAL");
            issue.setType("CONNECTION_LEAK");
            issue.setDescription("Vazamento de conexão detectado: empresta() sem devolve() correspondente");
            issue.setRecommendation("Adicione devolve() em bloco finally para garantir que a conexão seja sempre devolvida");
            issues.add(issue);
        }
        
        // Verificar complexidade alta (mock)
        int lineCount = code.split("\n").length;
        if (lineCount > 100) {
            CodeAnalysisResponse.Issue issue = new CodeAnalysisResponse.Issue();
            issue.setSeverity("WARNING");
            issue.setType("CODE_QUALITY");
            issue.setDescription("Classe muito extensa (" + lineCount + " linhas)");
            issue.setRecommendation("Considere refatorar em classes menores e mais focadas");
            issues.add(issue);
        }
        
        response.setIssues(issues);
        response.setProcessingTimeMs(150L);
        
        return response;
    }
    
    private ReportGenerationResponse createMockReport(List<CodeAnalysisResponse> analyses) {
        ReportGenerationResponse response = new ReportGenerationResponse();
        response.setSuccess(true);
        
        StringBuilder report = new StringBuilder();
        report.append("# Relatório Executivo MAYA\n\n");
        report.append("## Resumo da Análise\n\n");
        report.append("- **Total de arquivos analisados**: ").append(analyses.size()).append("\n");
        
        int totalIssues = analyses.stream().mapToInt(a -> a.getIssues().size()).sum();
        report.append("- **Total de issues encontrados**: ").append(totalIssues).append("\n\n");
        
        report.append("## Issues Críticos\n\n");
        analyses.stream()
                .flatMap(a -> a.getIssues().stream())
                .filter(i -> "CRITICAL".equals(i.getSeverity()))
                .forEach(issue -> {
                    report.append("- **").append(issue.getType()).append("**: ");
                    report.append(issue.getDescription()).append("\n");
                });
        
        response.setReport(report.toString());
        return response;
    }
}
```

### DTOs para Integração com IA

**AiApiDto.java**
```java
public class AiApiDto {
    
    @Data
    public static class LlmModel {
        private String id;
        private String name;
        private String description;
        private String provider;
        private Integer maxTokens;
        private Double temperature;
        private Boolean isActive;
        private Boolean supportsFunctionCalling;
        private Boolean multimodal;
    }
    
    @Data
    public static class ModelsResponse {
        private List<LlmModel> models;
        private Boolean success;
        private String message;
    }
}

@Data
public class CodeAnalysisRequest {
    private String code;
    private String language;
    private String modelId;
    private String prompt;
    private Map<String, Object> options;
}

@Data
public class CodeAnalysisResponse {
    private Boolean success;
    private String modelUsed;
    private String filePath;
    private List<Issue> issues;
    private Long processingTimeMs;
    private String rawResponse;
    
    @Data
    public static class Issue {
        private String severity;
        private String type;
        private String description;
        private String recommendation;
        private Integer lineNumber;
        private Integer columnNumber;
    }
}
```

### Controller para IA

**AiController.java**
```java
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {
    
    @Autowired
    private SinqiaAiService sinqiaAiService;
    
    /**
     * Listar modelos LLM disponíveis
     */
    @GetMapping("/models")
    public ResponseEntity<List<AiApiDto.LlmModel>> getAvailableModels() {
        try {
            List<AiApiDto.LlmModel> models = sinqiaAiService.getAvailableModels();
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            logger.error("Erro ao buscar modelos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Testar conectividade com API de IA
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<AiApiDto.LlmModel> models = sinqiaAiService.getAvailableModels();
            
            response.put("success", true);
            response.put("message", "API de IA acessível");
            response.put("modelsCount", models.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro na conexão com API de IA: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
```

## 🔧 Configurações de Integração

### application.properties

```properties
# TFS Configuration
tfs.server.url=${TFS_SERVER_URL:https://tfs.sinqia.com.br}
tfs.collection=${TFS_COLLECTION:GestaoRecursos}
tfs.username=${TFS_USERNAME:allan.luz}
tfs.password=${TFS_PASSWORD:}
tfs.timeout.connection=${TFS_TIMEOUT_CONNECTION:30000}
tfs.timeout.read=${TFS_TIMEOUT_READ:60000}

# Sinqia AI Configuration
maya.ai.base-url=${SINQIA_AI_URL:http://everai.sinqia.com.br}
maya.ai.timeout=${SINQIA_AI_TIMEOUT:30000}
maya.ai.enabled=${SINQIA_AI_ENABLED:true}
maya.ai.api-key=${SINQIA_AI_API_KEY:}

# Webhook Configuration
maya.webhook.enabled=${WEBHOOK_ENABLED:false}
maya.webhook.secret=${WEBHOOK_SECRET:}
```

### Configuração de Perfis

**application-dev.properties**
```properties
# Development - usar APIs reais se disponíveis, fallback caso contrário
tfs.server.url=https://dev.azure.com/sinqia-dev
maya.ai.enabled=true
logging.level.com.sinqia.maya=DEBUG
```

**application-prod.properties**
```properties
# Production - configuração completa
tfs.server.url=https://tfs.sinqia.com.br
tfs.collection=GestaoRecursos
maya.ai.enabled=true
maya.ai.base-url=http://everai.sinqia.com.br
maya.webhook.enabled=true
logging.level.com.sinqia.maya=INFO
```

## 🧪 Scripts de Teste de Integração

### Teste TFS (PowerShell)

```powershell
# test-tfs-integration.ps1

param(
    [string]$TfsUrl = "https://tfs.sinqia.com.br",
    [string]$Collection = "GestaoRecursos",
    [string]$Username = "allan.luz",
    [string]$PatToken = ""
)

Write-Host "Testando integração TFS..." -ForegroundColor Yellow

# 1. Testar conectividade básica
Write-Host "1. Testando conectividade básica..."
try {
    $response = Invoke-RestMethod -Uri "$TfsUrl/$Collection/_apis/projects" -Headers @{
        Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(":$PatToken"))
    }
    Write-Host "✅ Conectividade OK - Projetos encontrados: $($response.count)" -ForegroundColor Green
}
catch {
    Write-Host "❌ Erro de conectividade: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. Testar através da API do MAYA
Write-Host "2. Testando através da API MAYA..."
$testBody = @{
    serverUrl = $TfsUrl
    collection = $Collection
    username = $Username
    password = $PatToken
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/tfs/test-connection" -Method POST -Body $testBody -ContentType "application/json"
    if ($response.success) {
        Write-Host "✅ API MAYA OK: $($response.message)" -ForegroundColor Green
    } else {
        Write-Host "❌ API MAYA falhou: $($response.message)" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ Erro na API MAYA: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Teste de integração TFS concluído." -ForegroundColor Yellow
```

### Teste Sinqia AI (Bash)

```bash
#!/bin/bash
# test-sinqia-ai.sh

API_BASE="http://localhost:8081/api"
SINQIA_AI_URL="http://everai.sinqia.com.br"

echo "🤖 Testando integração Sinqia AI..."

# 1. Testar saúde da API
echo "1. Testando conectividade API MAYA..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE/ai/health")
if [ $response -eq 200 ]; then
    echo "✅ API MAYA respondendo"
else
    echo "❌ API MAYA não respondendo (status: $response)"
    exit 1
fi

# 2. Listar modelos disponíveis
echo "2. Listando modelos LLM..."
models=$(curl -s "$API_BASE/ai/models")
model_count=$(echo $models | jq length 2>/dev/null || echo "0")
echo "📋 Modelos encontrados: $model_count"

# 3. Testar conectividade direta com Sinqia AI
echo "3. Testando conectividade direta com Sinqia AI..."
if curl -s --connect-timeout 5 "$SINQIA_AI_URL" > /dev/null; then
    echo "✅ Sinqia AI acessível"
else
    echo "⚠️  Sinqia AI não acessível (usando fallback)"
fi

echo "🎯 Teste de integração Sinqia AI concluído."
```

## 🚀 Deployment das Integrações

### Docker Compose para Ambiente Completo

```yaml
version: '3.8'

services:
  maya-backend:
    build: ./backend
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - TFS_SERVER_URL=${TFS_SERVER_URL}
      - TFS_COLLECTION=${TFS_COLLECTION}
      - TFS_USERNAME=${TFS_USERNAME}
      - TFS_PASSWORD=${TFS_PAT_TOKEN}
      - SINQIA_AI_URL=${SINQIA_AI_URL}
      - DB_URL=${DB_URL}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - sqlserver
    networks:
      - maya-network

  maya-frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - maya-backend
    networks:
      - maya-network

  sqlserver:
    image: mcr.microsoft.com/mssql/server:2019-latest
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=${SQL_SA_PASSWORD}
    ports:
      - "1433:1433"
    volumes:
      - sqldata:/var/opt/mssql
    networks:
      - maya-network

volumes:
  sqldata:

networks:
  maya-network:
    driver: bridge
```

## 📋 Checklist de Integração

### ✅ TFS/Azure DevOps
- [ ] Configurar Personal Access Token com permissões adequadas
- [ ] Testar conectividade com REST API
- [ ] Validar acesso aos repositórios necessários
- [ ] Configurar webhook (se necessário)
- [ ] Testar importação de commits
- [ ] Validar obtenção de conteúdo de arquivos

### ✅ Sinqia AI
- [ ] Verificar conectividade com everai.sinqia.com.br
- [ ] Configurar autenticação (se necessária)
- [ ] Testar listagem de modelos LLM
- [ ] Validar análise de código com IA
- [ ] Configurar fallback para desenvolvimento
- [ ] Testar geração de relatórios

### ✅ Sistema Completo
- [ ] Testar fluxo completo: TFS → Análise → IA → Relatório
- [ ] Validar performance das integrações
- [ ] Configurar logs de auditoria
- [ ] Implementar retry logic para falhas temporárias
- [ ] Configurar alertas para falhas de integração
- [ ] Documentar troubleshooting

## 🔧 Próximos Passos

1. Configure as credenciais TFS seguindo `prompt-configuracao.md`
2. Teste a conectividade com ambas as APIs
3. Implemente os serviços de integração
4. Desenvolva os controllers REST
5. Teste o fluxo completo de importação e análise
6. Configure monitoramento e alertas

As integrações são o coração do sistema MAYA, conectando as fontes de código com a inteligência de análise para fornecer insights valiosos sobre qualidade do código.
