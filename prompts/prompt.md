# MAYA Code Review System - Prompt para Criação Completa

## 📋 Visão Geral do Sistema

O **MAYA Code Review System** é uma aplicação empresarial completa para análise automatizada de código integrada ao TFS/Azure DevOps, desenvolvida especificamente para os padrões de código da Sinqia. O sistema combina análise estática tradicional com inteligência artificial para detectar problemas críticos, especialmente vazamentos de conexão de banco de dados.

### 🎯 Objetivo Principal
Criar um sistema que automatize a revisão de código seguindo os padrões rigorosos da Sinqia, com foco especial na detecção de vazamentos de conexão (padrão `empresta()`/`devolve()`) e conformidade com as diretrizes de código da empresa.

### 🏗️ Arquitetura do Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  TFS/Azure      │    │   MAYA Web      │    │   SQL Server    │
│  DevOps         │◄──►│   Application   │◄──►│   Database      │
│  (Pull Requests)│    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │  Sinqia AI      │
                       │  Integration    │
                       └─────────────────┘
```

## 📚 Documentação de Referência

Este prompt está baseado na análise completa do projeto existente. Para detalhes específicos, consulte os arquivos auxiliares:

- `prompt-backend.md` - Especificação completa do backend Spring Boot
- `prompt-frontend.md` - Especificação completa do frontend Angular 18
- `prompt-database.md` - Schema e configuração do banco de dados
- `prompt-integracoes.md` - TFS e integração com IA da Sinqia
- `prompt-padroes-maya.md` - Algoritmos de análise MAYA específicos
- `prompt-configuracao.md` - Setup e deployment do sistema

## 🚀 Funcionalidades Principais

### ✅ Sistema de Análise MAYA
- **Detecção de vazamento de conexão**: Análise rigorosa do padrão `empresta()`/`devolve()`
- **Validação de padrões Sinqia**: Verificação automática de conformidade com coding standards
- **Análise de complexidade**: Cálculo de complexidade ciclomática e métricas de qualidade
- **Detecção de mudanças críticas**: Identificação de alterações em tipos, métodos e validações

### 📊 Dashboard e Relatórios
- **Interface moderna**: Angular 18 com PrimeNG e design responsivo
- **Métricas em tempo real**: Dashboard com estatísticas de qualidade por repositório
- **Filtros avançados**: Busca por autor, período, severidade e tipo de problema
- **Relatórios executivos**: Geração automática de relatórios em Markdown

### 🔗 Integração Empresarial
- **TFS/Azure DevOps**: Importação automática de commits e pull requests
- **Webhook Support**: Integração automática com eventos de repositório
- **Sinqia AI**: Seleção de modelos LLM para análise avançada
- **Configuração flexível**: Prompts personalizados por projeto

### 🛡️ Sistema de Qualidade
- **Análise multicamada**: Combinação de análise estática + IA
- **Severidade configurável**: INFO, WARNING, ERROR, CRITICAL
- **Histórico completo**: Rastreamento de evolução da qualidade
- **Aprovação automática**: Integração com políticas de branch

## 🛠️ Stack Tecnológica

### Backend (Spring Boot 3.2.0)
- **Framework**: Spring Boot com arquitetura RESTful
- **Persistência**: Spring Data JPA + SQL Server
- **Documentação**: SpringDoc OpenAPI (Swagger)
- **Build**: Maven com profiles de desenvolvimento/produção
- **Segurança**: Spring Security configurável
- **Monitoramento**: Spring Actuator

### Frontend (Angular 18)
- **Framework**: Angular com Standalone Components
- **UI Library**: PrimeNG + Angular Material
- **Styling**: SCSS com design system personalizado
- **Build**: Angular CLI com SSR (Server-Side Rendering)
- **Estado**: RxJS para programação reativa
- **Routing**: Lazy loading para otimização

### Banco de Dados (SQL Server)
- **Entidades principais**: CodeReview, FileAnalysis, AnalysisIssue
- **Configurações**: ConfigurationSettings, AuxiliaryFile
- **Índices otimizados**: Performance para consultas complexas
- **Suporte H2**: Ambiente de desenvolvimento

### Integrações
- **TFS/Azure DevOps**: REST API com autenticação PAT
- **Sinqia AI**: HTTP client para modelos LLM
- **File System**: Upload e processamento de arquivos auxiliares

## 📋 Pré-requisitos Técnicos

### Ambiente de Desenvolvimento
- **Java 17+** (OpenJDK ou Oracle JDK)
- **Node.js 18+** com npm 9+
- **Maven 3.8+** para build do backend
- **Angular CLI 18+** para desenvolvimento frontend
- **SQL Server 2019+** ou H2 para desenvolvimento
- **Git** para controle de versão
- **IDE**: IntelliJ IDEA, Eclipse ou VS Code

### Infraestrutura
- **Servidor de aplicação**: Tomcat embarcado (Spring Boot)
- **Banco de dados**: SQL Server com collation apropriada
- **TFS/Azure DevOps**: Acesso com Personal Access Token
- **Sinqia AI**: Conectividade com everai.sinqia.com.br

## 🎯 Objetivos do Sistema

### Para Desenvolvedores
1. **Feedback imediato** sobre qualidade do código
2. **Detecção precoce** de vazamentos de conexão
3. **Sugestões de melhoria** baseadas em IA
4. **Conformidade automática** com padrões Sinqia

### Para Líderes Técnicos
1. **Visibilidade completa** da qualidade do código
2. **Métricas objetivas** para tomada de decisão
3. **Relatórios executivos** para stakeholders
4. **Tendências de qualidade** ao longo do tempo

### Para a Organização
1. **Padronização** de processos de code review
2. **Redução de bugs** em produção
3. **Melhoria contínua** da qualidade do software
4. **Compliance** com políticas internas

## 📋 Planejamento de Implementação

A implementação completa segue um plano estruturado em 8 fases principais:

### Fase 1: Configuração do Ambiente (1-2 dias)
- Setup do ambiente de desenvolvimento
- Configuração do banco de dados
- Estruturação inicial do projeto

### Fase 2: Backend Core (3-5 dias)
- Implementação das entidades JPA
- Criação dos repositórios e serviços básicos
- Setup do Spring Boot com configurações

### Fase 3: Análise MAYA (4-6 dias)
- Implementação dos algoritmos de análise
- Sistema de detecção de vazamento de conexão
- Métricas de qualidade e complexidade

### Fase 4: Integração TFS (2-3 dias)
- Cliente HTTP para TFS/Azure DevOps
- Importação de commits e arquivos
- Processamento de webhooks

### Fase 5: Frontend Base (3-4 dias)
- Setup Angular 18 com PrimeNG
- Componentes básicos e roteamento
- Serviços HTTP e modelos

### Fase 6: Interface de Usuário (4-5 days)
- Dashboard com métricas
- Listagem e detalhes de reviews
- Formulários de configuração

### Fase 7: Integração IA (2-3 dias)
- Seleção de modelos LLM
- Análise híbrida (estática + IA)
- Configuração de prompts personalizados

### Fase 8: Finalização (2-3 dias)
- Testes de integração
- Documentação
- Deploy e configuração de produção

**Tempo total estimado**: 21-31 dias úteis para equipe de 2-3 desenvolvedores

## 🔧 Padrões e Convenções

### Padrões de Código Sinqia
O sistema implementa e verifica conformidade com os padrões específicos da Sinqia:

1. **Licença obrigatória** em todos os arquivos
2. **Formatação Allman style** para chaves
3. **Nomenclatura em português** (exceto termos técnicos)
4. **Logging estruturado** com templates específicos
5. **Gestão rigorosa** de conexões de banco (`empresta`/`devolve`)

### Algoritmos de Análise MAYA
- **Connection Leak Detection**: Padrão regex para `empresta()`/`devolve()`
- **Cyclomatic Complexity**: Análise de estruturas de controle
- **Code Quality Score**: Algoritmo proprietário baseado em múltiplas métricas
- **Security Analysis**: Detecção de padrões de risco crítico

## 📖 Próximos Passos

1. **Leia os arquivos auxiliares** para entender cada componente específico
2. **Configure o ambiente** seguindo prompt-configuracao.md
3. **Implemente o backend** seguindo prompt-backend.md
4. **Desenvolva o frontend** seguindo prompt-frontend.md
5. **Integre os sistemas** seguindo prompt-integracoes.md
6. **Teste e valide** todas as funcionalidades

## 🎯 Resultado Esperado

Ao final da implementação, você terá um sistema completo de code review que:

- ✅ Detecta automaticamente vazamentos de conexão
- ✅ Valida conformidade com padrões Sinqia
- ✅ Integra seamlessly com TFS/Azure DevOps
- ✅ Oferece interface moderna e intuitiva
- ✅ Suporta análise híbrida com IA
- ✅ Gera relatórios executivos automáticos
- ✅ Escalável para múltiplos projetos

**Sistema MAYA Code Review** - Automatizando a qualidade de código desde 2024 🚀

---

**Nota**: Este prompt principal fornece a visão geral. Para implementação específica de cada componente, consulte os arquivos auxiliares correspondentes.
