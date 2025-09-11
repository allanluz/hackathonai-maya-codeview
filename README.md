# 🚀 MAYA Code Review System

Sistema inteligente de análise de código com foco em detecção de vazamentos de conexão e conformidade com padrões Sinqia, integrado com Azure DevOps e serviços de IA.

## 📋 Visão Geral

O Sistema MAYA (Monitoring and Analysis for Your Applications) é uma solução completa para análise automática de código que:

- 🔍 **Detecta vazamentos de conexão** através de algoritmos especializados
- 📊 **Analisa qualidade de código** com métricas personalizadas
- 🎯 **Valida padrões Sinqia** de desenvolvimento
- 🤖 **Integra com IA** para análises semânticas avançadas
- 📈 **Gera relatórios executivos** automáticos
- 🔄 **Conecta com Azure DevOps** para análise contínua

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │     Backend     │    │   Integrations  │
│   Angular 18    │◄──►│  Spring Boot 3  │◄──►│  Azure DevOps   │
│   PrimeNG       │    │     Java 17     │    │   OpenAI API    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                ▲
                                │
                       ┌─────────────────┐
                       │    Database     │
                       │  SQL Server     │
                       │      H2         │
                       └─────────────────┘
```

## 🎯 Funcionalidades Principais

### Análise MAYA Core
- ✅ **Detecção de vazamentos**: Identifica `empresta()` sem `devolve()` correspondente
- ✅ **Complexidade ciclomática**: Calcula métricas de complexidade
- ✅ **Padrões arquiteturais**: Valida estrutura de pacotes Sinqia
- ✅ **Análise de segurança**: Detecta vulnerabilidades comuns
- ✅ **Score de qualidade**: Sistema de pontuação 0-100

### Integração com IA
- 🤖 **Análise semântica**: Compreensão de contexto via LLM
- 📝 **Relatórios executivos**: Geração automática de documentação
- 💡 **Sugestões inteligentes**: Recomendações de melhoria
- 🔄 **Processamento em lote**: Análise de múltiplos arquivos

### Azure DevOps Integration
- 🔄 **Sincronização automática**: Import de commits e PRs
- 📊 **Dashboard centralizado**: Métricas por repositório
- ⚡ **Análise em tempo real**: Triggered por webhooks
- 📈 **Histórico completo**: Tracking de evolução da qualidade

## 🛠️ Stack Tecnológica

### Backend
- **Spring Boot 3.2.0** - Framework principal
- **Java 17** - Linguagem de desenvolvimento
- **Spring Data JPA** - Persistência
- **SQL Server** - Banco de dados principal
- **H2** - Banco in-memory para desenvolvimento
- **Spring Security** - Autenticação e autorização
- **Maven** - Gerenciamento de dependências

### Frontend (Planejado)
- **Angular 18** - Framework frontend
- **PrimeNG** - Biblioteca de componentes
- **TypeScript** - Linguagem de desenvolvimento
- **RxJS** - Programação reativa
- **Chart.js** - Visualização de dados

### Infraestrutura
- **Docker** - Containerização
- **Docker Compose** - Orquestração local
- **Azure DevOps** - CI/CD
- **Prometheus** - Monitoramento
- **Grafana** - Dashboard de métricas

## 🚀 Quick Start

### Pré-requisitos
- Java 17+
- Docker Desktop
- Maven 3.8+
- Acesso ao Azure DevOps (opcional)
- Chave de API para IA (opcional)

### Instalação Rápida

```bash
# Clonar repositório
git clone <url-do-repositorio>
cd hackathonai-maya-codeview

# Executar com Docker (recomendado)
./maya.sh build
./maya.sh start

# Ou no Windows
.\maya.ps1 build
.\maya.ps1 start
```

### Verificar Instalação

Acesse os endpoints:
- 🌐 **API**: http://localhost:8080
- 💓 **Health Check**: http://localhost:8080/actuator/health
- 📊 **Métricas**: http://localhost:8080/actuator/metrics
- 📋 **Swagger**: http://localhost:8080/swagger-ui.html

## 📊 Dashboard e Métricas

### KPIs Principais
- **Quality Score**: Média de qualidade do código (0-100)
- **Critical Issues**: Número de problemas críticos
- **Connection Leaks**: Vazamentos detectados
- **Code Coverage**: Cobertura de testes
- **Technical Debt**: Estimativa de débito técnico

### Relatórios Disponíveis
- 📈 **Tendência de Qualidade** por período
- 🏆 **Ranking de Desenvolvedores** por score
- 📊 **Métricas por Repositório** detalhadas
- 🚨 **Alertas de Qualidade** configuráveis
- 📋 **Relatórios Executivos** automáticos

## 🔧 Configuração

### Variáveis de Ambiente

```bash
# Azure DevOps
TFS_BASE_URL=https://tfs.sinqia.com.br
TFS_ORGANIZATION=sinqia
TFS_PERSONAL_ACCESS_TOKEN=<seu-token>

# Serviço de IA
AI_ENDPOINT=https://api.openai.com/v1/chat/completions
AI_API_KEY=<sua-chave>

# Banco de Dados
DB_URL=jdbc:sqlserver://servidor:1433;databaseName=maya
DB_USERNAME=usuario
DB_PASSWORD=senha
```

### Perfis de Execução

- `dev` - Desenvolvimento local com H2
- `docker` - Execução em containers
- `prod` - Produção com SQL Server

## 📁 Estrutura do Projeto

```
hackathonai-maya-codeview/
├── prompts/                 # Documentação e especificações
├── backend/                 # Spring Boot application
│   ├── src/main/java/      # Código fonte Java
│   ├── src/main/resources/ # Configurações e recursos
│   └── Dockerfile          # Container backend
├── frontend/               # Angular application (futuro)
├── docs/                   # Documentação adicional
├── docker-compose.yml      # Orquestração Docker
├── maya.sh                 # Script de automação (Linux/Mac)
├── maya.ps1                # Script de automação (Windows)
└── README.md              # Este arquivo
```

## 🧪 Testes

```bash
# Executar todos os testes
./maya.sh test

# Testes com cobertura
cd backend
./mvnw test jacoco:report

# Testes de integração
./mvnw integration-test
```

## 🔄 CI/CD Pipeline

```yaml
# Pipeline stages
Build → Test → Security Scan → Deploy → Monitor

# Deployment targets
- DEV: Automático em PRs
- QA: Manual após merge
- PROD: Aprovação obrigatória
```

## 📈 Roadmap

### Fase 1 - Core ✅
- [x] Backend Spring Boot
- [x] Algoritmos MAYA
- [x] Integração Azure DevOps
- [x] API REST completa

### Fase 2 - Frontend 🚧
- [ ] Interface Angular
- [ ] Dashboard interativo
- [ ] Configuração via UI
- [ ] Relatórios visuais

### Fase 3 - IA Avançada 📋
- [ ] ML para detecção de padrões
- [ ] Correção automática
- [ ] Análise preditiva
- [ ] Recomendações personalizadas

### Fase 4 - Enterprise 🎯
- [ ] Multi-tenancy
- [ ] SSO/LDAP
- [ ] Auditoria completa
- [ ] High Availability

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma feature branch (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

### Padrões de Desenvolvimento
- ✅ Seguir convenções Java/Spring Boot
- ✅ Cobertura de testes > 80%
- ✅ Documentação atualizada
- ✅ Code review obrigatório

## 📞 Suporte

- 📧 **Email**: equipe-maya@sinqia.com.br
- 💬 **Teams**: Canal #maya-support
- 📚 **Wiki**: Confluence Sinqia
- 🐛 **Issues**: Azure DevOps

## 📄 Licença

Copyright © 2024 Sinqia S.A. Todos os direitos reservados.

Este software é propriedade da Sinqia e seu uso está restrito aos colaboradores autorizados.

---

## 🏷️ Tags

`code-review` `java` `spring-boot` `azure-devops` `ai` `sinqia` `quality-assurance` `static-analysis` `connection-leaks` `automation`

---

**Sistema MAYA - Elevando a Qualidade do Código Sinqia** 🚀

*Desenvolvido com ❤️ pela equipe Sinqia*
