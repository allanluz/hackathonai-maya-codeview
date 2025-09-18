package com.sinqia.maya.dto;

import java.util.List;

/**
 * DTOs para integracao com Sinqia AI API
 * Baseado na documentacao da API em http://everai.sinqia.com.br/docs/#/
 */
public class AiApiDto {

    /**
     * Resposta da API para listar modelos LLM disponiveis
     */
    public static class LlmModelsResponse {
        private List<LlmModel> models;
        private int total;
        private String status;

        // Constructors
        public LlmModelsResponse() {}

        public LlmModelsResponse(List<LlmModel> models, int total, String status) {
            this.models = models;
            this.total = total;
            this.status = status;
        }

        // Getters and Setters
        public List<LlmModel> getModels() {
            return models;
        }

        public void setModels(List<LlmModel> models) {
            this.models = models;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Modelo LLM disponivel na API
     */
    public static class LlmModel {
        private String id;
        private String name;
        private String description;
        private String provider;
        private Integer maxTokens;
        private Boolean isActive;
        private Double temperature;
        private Double topP;
        private ModelCapabilities capabilities;
        private PricingInfo pricing;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }

        public ModelCapabilities getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(ModelCapabilities capabilities) {
            this.capabilities = capabilities;
        }

        public PricingInfo getPricing() {
            return pricing;
        }

        public void setPricing(PricingInfo pricing) {
            this.pricing = pricing;
        }
    }

    /**
     * Capacidades especificas do modelo
     */
    public static class ModelCapabilities {
        private Boolean codeGeneration;
        private Boolean codeReview;
        private Boolean documentation;
        private Boolean translation;
        private Boolean debugging;

        // Getters and Setters
        public Boolean getCodeGeneration() {
            return codeGeneration;
        }

        public void setCodeGeneration(Boolean codeGeneration) {
            this.codeGeneration = codeGeneration;
        }

        public Boolean getCodeReview() {
            return codeReview;
        }

        public void setCodeReview(Boolean codeReview) {
            this.codeReview = codeReview;
        }

        public Boolean getDocumentation() {
            return documentation;
        }

        public void setDocumentation(Boolean documentation) {
            this.documentation = documentation;
        }

        public Boolean getTranslation() {
            return translation;
        }

        public void setTranslation(Boolean translation) {
            this.translation = translation;
        }

        public Boolean getDebugging() {
            return debugging;
        }

        public void setDebugging(Boolean debugging) {
            this.debugging = debugging;
        }
    }

    /**
     * Informacoes de preco do modelo
     */
    public static class PricingInfo {
        private Double inputTokenPrice;
        private Double outputTokenPrice;
        private String currency;

        // Getters and Setters
        public Double getInputTokenPrice() {
            return inputTokenPrice;
        }

        public void setInputTokenPrice(Double inputTokenPrice) {
            this.inputTokenPrice = inputTokenPrice;
        }

        public Double getOutputTokenPrice() {
            return outputTokenPrice;
        }

        public void setOutputTokenPrice(Double outputTokenPrice) {
            this.outputTokenPrice = outputTokenPrice;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    /**
     * Requisicao para analise de codigo
     */
    public static class CodeAnalysisRequest {
        private String code;
        private String language;
        private String model;
        private String analysisType;
        private String filePath;
        private AnalysisOptions options;

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getAnalysisType() {
            return analysisType;
        }

        public void setAnalysisType(String analysisType) {
            this.analysisType = analysisType;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public AnalysisOptions getOptions() {
            return options;
        }

        public void setOptions(AnalysisOptions options) {
            this.options = options;
        }
    }

    public static class AnalysisOptions {
        private Boolean deepAnalysis;
        private Boolean securityCheck;
        private Boolean performanceCheck;
        private Boolean documentationCheck;

        // Getters and Setters
        public Boolean getDeepAnalysis() {
            return deepAnalysis;
        }

        public void setDeepAnalysis(Boolean deepAnalysis) {
            this.deepAnalysis = deepAnalysis;
        }

        public Boolean getSecurityCheck() {
            return securityCheck;
        }

        public void setSecurityCheck(Boolean securityCheck) {
            this.securityCheck = securityCheck;
        }

        public Boolean getPerformanceCheck() {
            return performanceCheck;
        }

        public void setPerformanceCheck(Boolean performanceCheck) {
            this.performanceCheck = performanceCheck;
        }

        public Boolean getDocumentationCheck() {
            return documentationCheck;
        }

        public void setDocumentationCheck(Boolean documentationCheck) {
            this.documentationCheck = documentationCheck;
        }
    }

    /**
     * Resposta da analise de codigo
     */
    public static class CodeAnalysisResponse {
        private String status;
        private String filePath;
        private String language;
        private String model;
        private String summary;
        private Double score;
        private List<Issue> issues;
        private Metrics metrics;

        // Getters e Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public List<Issue> getIssues() {
            return issues;
        }

        public void setIssues(List<Issue> issues) {
            this.issues = issues;
        }

        public Metrics getMetrics() {
            return metrics;
        }

        public void setMetrics(Metrics metrics) {
            this.metrics = metrics;
        }
    }

    /**
     * Issue encontrado na analise
     */
    public static class Issue {
        private String type;
        private String severity;
        private String message;
        private String suggestion;
        private Integer line;
        private Integer column;
        private String category;

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }

        public Integer getLine() {
            return line;
        }

        public void setLine(Integer line) {
            this.line = line;
        }

        public Integer getColumn() {
            return column;
        }

        public void setColumn(Integer column) {
            this.column = column;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    /**
     * Metricas da analise
     */
    public static class Metrics {
        private Integer linesOfCode;
        private Integer complexity;
        private Integer maintainabilityIndex;
        private Double testCoverage;

        // Getters and Setters
        public Integer getLinesOfCode() {
            return linesOfCode;
        }

        public void setLinesOfCode(Integer linesOfCode) {
            this.linesOfCode = linesOfCode;
        }

        public Integer getComplexity() {
            return complexity;
        }

        public void setComplexity(Integer complexity) {
            this.complexity = complexity;
        }

        public Integer getMaintainabilityIndex() {
            return maintainabilityIndex;
        }

        public void setMaintainabilityIndex(Integer maintainabilityIndex) {
            this.maintainabilityIndex = maintainabilityIndex;
        }

        public Double getTestCoverage() {
            return testCoverage;
        }

        public void setTestCoverage(Double testCoverage) {
            this.testCoverage = testCoverage;
        }
    }

    /**
     * Requisicao para analise em lote
     */
    public static class BatchAnalysisRequest {
        private List<CodeAnalysisRequest> files;
        private String model;
        private AnalysisOptions options;

        // Getters and Setters
        public List<CodeAnalysisRequest> getFiles() {
            return files;
        }

        public void setFiles(List<CodeAnalysisRequest> files) {
            this.files = files;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public AnalysisOptions getOptions() {
            return options;
        }

        public void setOptions(AnalysisOptions options) {
            this.options = options;
        }
    }
}
