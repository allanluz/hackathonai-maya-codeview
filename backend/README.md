# MAYA Code Review System - Backend

Sistema de análise de código com foco em detecção de vazamentos de conexão e conformidade com padrões Sinqia.

## 🚀 Tecnologias

- **Spring Boot 3.2.0** - Framework principal
- **Java 17** - Linguagem de desenvolvimento
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória (desenvolvimento)
- **SQL Server** - Banco de dados (produção)
- **Spring Security** - Segurança e autenticação
- **Maven** - Gerenciamento de dependências

## 📁 Estrutura do Projeto

```
backend/
├── src/main/java/com/sinqia/maya/
│   ├── MayaCodeReviewApplication.java    # Classe principal
│   ├── config/                           # Configurações
│   │   ├── CacheConfiguration.java
│   │   ├── CorsConfiguration.java
│   │   └── RestTemplateConfiguration.java
│   ├── controller/                       # Controllers REST
│   │   ├── CodeReviewController.java
│   │   ├── ConfigurationController.java
│   │   └── TfsController.java
│   ├── entity/                           # Entidades JPA
│   │   ├── AnalysisIssue.java
│   │   ├── AuxiliaryFile.java
│   │   ├── CodeReview.java
│   │   ├── ConfigurationSettings.java
│   │   └── FileAnalysis.java
│   ├── repository/                       # Repositórios de dados
│   │   ├── impl/
│   │   │   └── CodeReviewRepositoryImpl.java
│   │   ├── AnalysisIssueRepository.java
│   │   ├── AuxiliaryFileRepository.java
│   │   ├── CodeReviewRepository.java
│   │   ├── ConfigurationSettingsRepository.java
│   │   └── FileAnalysisRepository.java
│   └── service/                          # Serviços de negócio
│       ├── ConfigurationService.java
│       ├── MayaAnalysisService.java
│       ├── SinqiaAiService.java
│       └── TfsService.java
├── src/main/resources/
│   ├── application.properties           # Configurações principais
│   ├── application-dev.properties       # Perfil desenvolvimento
│   ├── application-prod.properties      # Perfil produção
│   └── data.sql                        # Dados iniciais
└── pom.xml                             # Dependências Maven
```

## 🔧 Configuração

### Pré-requisitos
- Java 17 ou superior
- Maven 3.8+
- Acesso ao Azure DevOps/TFS (opcional)
- Chave de API para serviços de IA (opcional)

### Variáveis de Ambiente

```bash
# Azure DevOps/TFS
TFS_BASE_URL=https://tfs.sinqia.com.br
TFS_ORGANIZATION=sinqia
TFS_PERSONAL_ACCESS_TOKEN=seu_token_aqui

# Serviço de IA
AI_ENDPOINT=https://api.openai.com/v1/chat/completions
AI_API_KEY=sua_chave_aqui

# Banco de dados (produção)
DB_URL=jdbc:sqlserver://servidor:1433;databaseName=maya
DB_USERNAME=usuario
DB_PASSWORD=senha
```

## 🏃‍♂️ Executando

### Desenvolvimento
```bash
# Clonar repositório
git clone <url-do-repositorio>
cd maya-codeview/backend

# Executar com perfil de desenvolvimento
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ou compilar e executar
./mvnw clean package
java -jar target/maya-code-review-1.0.0.jar --spring.profiles.active=dev
```

### Produção
```bash
# Configurar variáveis de ambiente
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:sqlserver://...
export DB_USERNAME=usuario
export DB_PASSWORD=senha

# Executar
java -jar maya-code-review-1.0.0.jar
```

## 📡 APIs

### Análises de Código
- `GET /api/v1/code-reviews` - Listar análises
- `GET /api/v1/code-reviews/{id}` - Detalhes de análise
- `POST /api/v1/code-reviews/analyze` - Executar análise manual
- `GET /api/v1/code-reviews/dashboard/stats` - Estatísticas do dashboard

### Integração TFS
- `GET /api/v1/tfs/test-connection` - Testar conectividade
- `GET /api/v1/tfs/projects/{project}/repositories` - Listar repositórios
- `POST /api/v1/tfs/projects/{project}/repositories/{repo}/commits/{sha}/analyze` - Analisar commit

### Configurações
- `GET /api/v1/configurations` - Listar configurações
- `POST /api/v1/configurations/validate` - Validar configurações
- `PUT /api/v1/configurations/tfs` - Atualizar configurações TFS
- `PUT /api/v1/configurations/ai` - Atualizar configurações IA

## 🔍 Algoritmos MAYA

### Detecção de Vazamentos de Conexão
O sistema identifica automaticamente:
- Chamadas `empresta()` sem `devolve()` correspondente
- Desequilíbrios em blocos try/catch/finally
- Múltiplas conexões não devolvidas
- Padrões de vazamento conhecidos

### Análise de Complexidade
- Complexidade ciclomática
- Profundidade de aninhamento
- Número de linhas por método
- Métricas de manutenibilidade

### Conformidade Sinqia
- Estrutura de pacotes padrão
- Nomenclatura de classes e métodos
- Padrões arquiteturais
- Boas práticas de segurança

## 🎯 Métricas de Qualidade

### Score MAYA (0-100)
- **90-100**: Excelente qualidade
- **70-89**: Boa qualidade
- **50-69**: Qualidade média
- **< 50**: Necessita melhorias

### Severidade de Issues
- **CRITICAL**: Vazamentos de conexão, vulnerabilidades
- **ERROR**: Violações de padrões obrigatórios
- **WARNING**: Problemas de qualidade
- **INFO**: Sugestões de melhoria

## 🔄 Integração com IA

O sistema integra com serviços de LLM para:
- Análise semântica de código
- Geração de relatórios executivos
- Sugestões inteligentes de correção
- Detecção de padrões complexos

## 📊 Dashboard e Relatórios

### Métricas Disponíveis
- Score médio por repositório
- Issues críticos por período
- Top desenvolvedores por qualidade
- Tendências de qualidade
- Relatórios executivos automáticos

## 🔒 Segurança

- Autenticação via tokens
- CORS configurado para frontend
- Logs de auditoria
- Validação de entrada
- Tratamento seguro de credenciais

## 🧪 Testes

```bash
# Executar testes unitários
./mvnw test

# Executar testes com cobertura
./mvnw test jacoco:report

# Executar testes de integração
./mvnw integration-test
```

## 📝 Logs

O sistema gera logs estruturados em:
- Console (desenvolvimento)
- Arquivo `/var/log/maya/` (produção)
- Níveis: ERROR, WARN, INFO, DEBUG

## 🚨 Monitoramento

### Health Checks
- `GET /actuator/health` - Status da aplicação
- `GET /actuator/info` - Informações da aplicação
- `GET /actuator/metrics` - Métricas do sistema

### Alertas
- Configurações inválidas
- Falhas de conectividade
- Performance degradada
- Erros críticos

## 🔧 Troubleshooting

### Problemas Comuns

**Erro de conexão com TFS**
```
Verifique as configurações em /api/v1/configurations/tfs
Valide o token de acesso pessoal
Teste conectividade em /api/v1/tfs/test-connection
```

**Falha na análise de IA**
```
Verifique chave de API em /api/v1/configurations/ai
Confirme endpoint e modelo configurados
Monitore logs para detalhes do erro
```

**Performance lenta**
```
Verifique índices do banco de dados
Monitore uso de memória
Ajuste tamanhos de cache
```

## 📈 Roadmap

- [ ] Análise de múltiplas linguagens
- [ ] Integração com GitHub/GitLab
- [ ] Machine Learning para detecção
- [ ] Correção automática de code smells
- [ ] Dashboard em tempo real
- [ ] Notificações automáticas

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma feature branch
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto é propriedade da Sinqia S.A. Todos os direitos reservados.

---

**Sistema MAYA - Mantendo a Qualidade do Código Sinqia** 🚀
