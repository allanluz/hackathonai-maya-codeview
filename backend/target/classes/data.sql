-- MAYA Code Review System - Dados Iniciais
-- Configurações padrão e dados de exemplo para desenvolvimento

-- ===================================================================
-- CONFIGURAÇÕES DO SISTEMA
-- ===================================================================

-- Configurações TFS
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('tfs.server.url', 'https://tfs.sinqia.com.br', 'STRING', 'TFS', 'URL do servidor TFS/Azure DevOps', false, true),
('tfs.collection', 'GestaoRecursos', 'STRING', 'TFS', 'Collection do TFS', false, true),
('tfs.username', 'allan.luz', 'STRING', 'TFS', 'Usuário para autenticação TFS', false, true),
('tfs.timeout.connection', '30000', 'INTEGER', 'TFS', 'Timeout de conexão TFS em milissegundos', false, false),
('tfs.timeout.read', '60000', 'INTEGER', 'TFS', 'Timeout de leitura TFS em milissegundos', false, false);

-- Configurações MAYA Analysis
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.analysis.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar análise MAYA', false, true),
('maya.analysis.connection-leak.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar detecção de vazamento de conexão', false, false),
('maya.analysis.complexity.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar análise de complexidade', false, false),
('maya.analysis.security.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar análise de segurança', false, false),
('maya.analysis.complexity.threshold.high', '15', 'INTEGER', 'ANALYSIS', 'Limite para complexidade alta', false, false),
('maya.analysis.complexity.threshold.critical', '20', 'INTEGER', 'ANALYSIS', 'Limite para complexidade crítica', false, false);

-- Configurações Sinqia AI
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.ai.enabled', 'true', 'BOOLEAN', 'AI', 'Habilitar integração com IA da Sinqia', false, false),
('maya.ai.base-url', 'http://everai.sinqia.com.br', 'URL', 'AI', 'URL base da API Sinqia AI', false, false),
('maya.ai.timeout', '30000', 'INTEGER', 'AI', 'Timeout para chamadas de IA em milissegundos', false, false),
('maya.ai.default-model', 'gpt-4', 'STRING', 'AI', 'Modelo LLM padrão para análise', false, false);

-- Configurações de Severidade
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.severity.connection-leak.missing-devolve', 'CRITICAL', 'STRING', 'SEVERITY', 'Severidade para empresta() sem devolve()', false, false),
('maya.severity.connection-leak.not-in-finally', 'ERROR', 'STRING', 'SEVERITY', 'Severidade para devolve() fora do finally', false, false),
('maya.severity.complexity.high', 'WARNING', 'STRING', 'SEVERITY', 'Severidade para alta complexidade', false, false),
('maya.severity.security.sql-injection', 'CRITICAL', 'STRING', 'SEVERITY', 'Severidade para SQL injection', false, false);

-- Configurações de Performance
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.performance.parallel-processing', 'true', 'BOOLEAN', 'PERFORMANCE', 'Habilitar processamento paralelo', false, false),
('maya.performance.max-threads', '10', 'INTEGER', 'PERFORMANCE', 'Número máximo de threads para análise', false, false),
('maya.performance.batch-size', '50', 'INTEGER', 'PERFORMANCE', 'Tamanho do lote para processamento', false, false),
('maya.performance.timeout.analysis', '300000', 'INTEGER', 'PERFORMANCE', 'Timeout para análise completa em milissegundos', false, false);

-- ===================================================================
-- ARQUIVOS AUXILIARES
-- ===================================================================

-- Documentação de padrões Sinqia
INSERT INTO auxiliary_files (name, content, file_type, description, is_active, version, tags) VALUES
('padroes-sinqia.md', 
'# Padrões de Desenvolvimento Sinqia

## ?? Gestão de Conexões (CRÍTICO)

### Regra Fundamental
**TODA** chamada `empresta()` DEVE ter `devolve()` correspondente em bloco `finally`.

### ? Exemplo Correto
```java
Connection conn = null;
try {
    conn = ConexaoFactory.empresta();
    // usar conexão
    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tabela WHERE id = ?");
    stmt.setInt(1, id);
    ResultSet rs = stmt.executeQuery();
    // processar resultados
} finally {
    if (conn != null) {
        ConexaoFactory.devolve(conn);
    }
}
```

### ? Exemplos Incorretos
```java
// ERRO: return antes de devolve()
Connection conn = ConexaoFactory.empresta();
if (condicao) {
    return; // VAZAMENTO!
}
ConexaoFactory.devolve(conn);

// ERRO: devolve() fora do finally
Connection conn = ConexaoFactory.empresta();
try {
    // usar conexão
} catch (Exception e) {
    // se houver exceção, devolve() não será executado
}
ConexaoFactory.devolve(conn); // VAZAMENTO!
```

## ?? Nomenclatura

### Classes
- **PascalCase**: `UsuarioService`, `RelatorioController`
- **Nomes em português**: Usar termos do domínio da aplicação
- **Sufixos obrigatórios**:
  - Services: `*Service`
  - Controllers: `*Controller`
  - Repositories: `*Repository` ou `*Dao`

### Métodos e Variáveis
- **camelCase**: `buscarUsuario()`, `nomeCompleto`
- **Verbos para métodos**: `salvar()`, `buscar()`, `validar()`
- **Substantivos para variáveis**: `usuario`, `listaRelatorios`

### Constantes
- **UPPER_SNAKE_CASE**: `TIMEOUT_PADRAO`, `MENSAGEM_ERRO`

## ??? Estrutura de Pacotes
```
com.sinqia.[produto].[modulo].[camada]
```

### Exemplos
- `com.sinqia.gestao.usuario.service`
- `com.sinqia.relatorio.financeiro.dao`
- `com.sinqia.comum.util`

### Camadas Obrigatórias
- `dao` - Acesso a dados
- `dto` - Objetos de transferência
- `service` - Lógica de negócio
- `controller` - Controladores web
- `util` - Utilitários

## ?? Segurança

### SQL Injection
```java
// ? CORRETO - PreparedStatement
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuario WHERE login = ?");
stmt.setString(1, login);

// ? INCORRETO - Concatenação
String sql = "SELECT * FROM usuario WHERE login = ''" + login + "''"; // VULNERÁVEL!
```

### Logs Seguros
```java
// ? CORRETO
logger.info("Login realizado para usuário: {}", usuario.getId());

// ? INCORRETO
logger.info("Login: " + login + ", Senha: " + senha); // VAZA SENHA!
```

## ?? Métricas de Qualidade

### Complexidade Ciclomática
- **Baixa**: 1-5 (Ótimo)
- **Média**: 6-10 (Aceitável)
- **Alta**: 11-15 (Revisar)
- **Crítica**: >15 (Refatorar obrigatório)

### Tamanho de Métodos
- **Máximo**: 50 linhas
- **Recomendado**: 20 linhas
- **Ideal**: 10 linhas

### Tamanho de Classes
- **Máximo**: 500 linhas
- **Recomendado**: 200 linhas

## ?? Boas Práticas

### 1. Tratamento de Exceções
```java
try {
    // código que pode falhar
} catch (SpecificException e) {
    logger.error("Erro específico: {}", e.getMessage());
    throw new BusinessException("Mensagem amigável", e);
} catch (Exception e) {
    logger.error("Erro inesperado", e);
    throw new SystemException("Erro interno", e);
}
```

### 2. Validações
```java
public void salvarUsuario(Usuario usuario) {
    if (usuario == null) {
        throw new IllegalArgumentException("Usuário não pode ser nulo");
    }
    if (StringUtils.isBlank(usuario.getNome())) {
        throw new ValidationException("Nome é obrigatório");
    }
    // continuar validações
}
```

### 3. Documentação
```java
/**
 * Busca usuário ativo por ID.
 * 
 * @param id ID do usuário (obrigatório)
 * @return Usuario encontrado
 * @throws UsuarioNotFoundException se não encontrado
 * @throws IllegalArgumentException se ID inválido
 */
public Usuario buscarPorId(Long id) {
    // implementação
}
```

## ?? Checklist de Code Review

### ? Obrigatório
- [ ] Licença Sinqia no cabeçalho
- [ ] Conexões balanceadas (empresta/devolve)
- [ ] Nomenclatura em português
- [ ] Estrutura de pacotes correta
- [ ] Tratamento de exceções
- [ ] Logs não sensíveis
- [ ] PreparedStatement para SQL
- [ ] Javadoc em métodos públicos

### ?? Recomendado
- [ ] Complexidade baixa (<10)
- [ ] Métodos pequenos (<20 linhas)
- [ ] Classes focadas (responsabilidade única)
- [ ] Testes unitários
- [ ] Validação de parâmetros
- [ ] Constantes para strings mágicas
',
'DOCUMENTATION', 
'Padrões de desenvolvimento e code review da Sinqia', 
true, 
'1.0', 
'sinqia,padroes,desenvolvimento,conexao,qualidade');

-- Prompt padrão para análise MAYA
INSERT INTO auxiliary_files (name, content, file_type, description, is_active, version, tags) VALUES
('maya-analysis-prompt.txt',
'Você é um especialista em revisão de código focado nos padrões MAYA da Sinqia.

INSTRUÇÕES DE ANÁLISE:

1. GESTÃO DE CONEXÕES (PRIORIDADE MÁXIMA):
   - Verificar empresta()/devolve() balanceados
   - Identificar vazamentos de conexão
   - Validar uso de try-finally
   - Detectar return/throw antes de devolve()

2. PADRÕES SINQIA:
   - Nomenclatura em português
   - Estrutura de pacotes com.sinqia.*
   - Suffixos obrigatórios (Service, Controller, etc)
   - Licença no cabeçalho

3. QUALIDADE DE CÓDIGO:
   - Complexidade ciclomática
   - Tamanho de métodos e classes
   - Código duplicado
   - Padrões de formatação

4. SEGURANÇA:
   - SQL injection (concatenação vs PreparedStatement)
   - Dados sensíveis em logs
   - Tratamento adequado de exceções
   - Validação de entrada

5. ARQUITETURA:
   - Separação de responsabilidades
   - Uso correto de camadas
   - Dependências apropriadas

FORMATO DE RESPOSTA:
Retorne análise em formato JSON:
{
  "issues": [
    {
      "severity": "CRITICAL|ERROR|WARNING|INFO",
      "type": "CONNECTION_LEAK|SECURITY_ISSUE|CODE_QUALITY|etc",
      "title": "Título conciso",
      "description": "Descrição detalhada",
      "lineNumber": 123,
      "suggestion": "Como corrigir"
    }
  ]
}

CRITÉRIOS DE SEVERIDADE:
- CRITICAL: Vazamentos de conexão, SQL injection
- ERROR: Violações graves de padrão, bugs evidentes
- WARNING: Complexidade alta, más práticas
- INFO: Sugestões de melhoria, otimizações',
'PROMPT',
'Prompt padrão para análise MAYA com IA',
true,
'1.0',
'maya,prompt,ia,analise');

-- Template de relatório
INSERT INTO auxiliary_files (name, content, file_type, description, is_active, version, tags) VALUES
('relatorio-executivo.md',
'# Relatório Executivo MAYA - {{repositorio}}

**Período:** {{dataInicio}} - {{dataFim}}
**Gerado em:** {{dataGeracao}}

## ?? Resumo Executivo

- **Total de arquivos analisados:** {{totalArquivos}}
- **Issues encontrados:** {{totalIssues}}
- **Issues críticos:** {{issuesCriticos}}
- **Score médio de qualidade:** {{scoreMedia}}/100

## ?? Issues Críticos

{{#issuesCriticos}}
### {{titulo}}
- **Arquivo:** `{{arquivo}}`
- **Linha:** {{linha}}
- **Descrição:** {{descricao}}
- **Impacto:** {{impacto}}
- **Recomendação:** {{recomendacao}}

{{/issuesCriticos}}

## ?? Métricas de Qualidade

### Distribuição de Issues
| Severidade | Quantidade | Percentual |
|------------|------------|------------|
| Crítico    | {{criticos}} | {{percentualCriticos}}% |
| Alto       | {{altos}} | {{percentualAltos}}% |
| Médio      | {{medios}} | {{percentualMedios}}% |
| Baixo      | {{baixos}} | {{percentualBaixos}}% |

### Top 5 Tipos de Issues
{{#topIssues}}
1. **{{tipo}}:** {{quantidade}} ocorrências
{{/topIssues}}

### Arquivos com Mais Issues
{{#arquivosProblematicos}}
1. **{{arquivo}}:** {{issues}} issues ({{criticos}} críticos)
{{/arquivosProblematicos}}

## ?? Análise de Conexões

- **Arquivos com vazamentos:** {{arquivosComVazamentos}}
- **Total de vazamentos detectados:** {{totalVazamentos}}
- **Taxa de conformidade:** {{taxaConformidade}}%

### Detalhes dos Vazamentos
{{#vazamentos}}
- **{{arquivo}}** (linha {{linha}}): {{descricao}}
{{/vazamentos}}

## ??? Conformidade Arquitetural

- **Padrões Sinqia:** {{conformidadePadroes}}%
- **Estrutura de pacotes:** {{conformidadePacotes}}%
- **Nomenclatura:** {{conformidadeNomes}}%

## ?? Recomendações

### Ações Imediatas (Críticas)
{{#acoesCriticas}}
- {{acao}}
{{/acoesCriticas}}

### Melhorias de Médio Prazo
{{#melhorias}}
- {{melhoria}}
{{/melhorias}}

### Sugestões de Longo Prazo
{{#sugestoes}}
- {{sugestao}}
{{/sugestoes}}

## ?? Checklist de Qualidade

- [ ] Todos os vazamentos de conexão corrigidos
- [ ] Issues críticos resolvidos
- [ ] Complexidade ciclomática dentro dos limites
- [ ] Padrões de nomenclatura seguidos
- [ ] Documentação adequada
- [ ] Testes unitários criados/atualizados

---
*Relatório gerado automaticamente pelo Sistema MAYA v{{versaoMaya}}*',
'TEMPLATE',
'Template para geração de relatórios executivos',
true,
'1.0',
'relatorio,template,executivo');

-- ===================================================================
-- DADOS DE EXEMPLO PARA DESENVOLVIMENTO
-- ===================================================================

-- Code Review de exemplo
INSERT INTO code_reviews (commit_sha, repository_name, project_name, author, title, status, critical_issues, total_issues, analysis_score, created_at) VALUES
('abc123def456', 'sistema-exemplo', 'Projeto Exemplo', 'joão.silva', 'Implementação inicial do serviço de usuários', 'COMPLETED', 2, 8, 75.5, NOW() - INTERVAL 1 DAY),
('def456ghi789', 'sistema-exemplo', 'Projeto Exemplo', 'maria.santos', 'Correção de vazamentos de conexão', 'COMPLETED', 0, 3, 92.0, NOW() - INTERVAL 2 HOUR),
('ghi789jkl012', 'api-financeiro', 'Sistema Financeiro', 'pedro.costa', 'Nova funcionalidade de relatórios', 'PENDING', 1, 5, 68.2, NOW() - INTERVAL 30 MINUTE);

-- File Analysis de exemplo
INSERT INTO file_analyses (code_review_id, file_path, class_name, language, line_count, complexity_score, connection_imbalance, score, connection_empresta, connection_devolve, connection_balanced, created_at) VALUES
(1, '/src/main/java/com/sinqia/exemplo/service/UsuarioService.java', 'UsuarioService', 'java', 145, 8.5, 0.0, 85.0, 3, 3, true, NOW() - INTERVAL 1 DAY),
(1, '/src/main/java/com/sinqia/exemplo/dao/UsuarioDao.java', 'UsuarioDao', 'java', 89, 12.0, 33.3, 65.0, 2, 1, false, NOW() - INTERVAL 1 DAY),
(2, '/src/main/java/com/sinqia/exemplo/dao/UsuarioDao.java', 'UsuarioDao', 'java', 92, 11.5, 0.0, 88.0, 2, 2, true, NOW() - INTERVAL 2 HOUR);

-- Analysis Issues de exemplo
INSERT INTO analysis_issues (file_analysis_id, severity, type, title, description, line_number, suggestion, created_at) VALUES
(1, 'WARNING', 'COMPLEXITY', 'Alta complexidade ciclomática', 'Método buscarUsuarios() possui complexidade de 12 (limite: 10)', 45, 'Considere dividir o método em funções menores', NOW() - INTERVAL 1 DAY),
(2, 'CRITICAL', 'CONNECTION_LEAK', 'Vazamento de conexão detectado', 'empresta() na linha 23 sem devolve() correspondente', 23, 'Adicione devolve() em bloco finally', NOW() - INTERVAL 1 DAY),
(2, 'ERROR', 'STYLE_VIOLATION', 'Violação de padrão de nomenclatura', 'Variável user_name deveria ser userName (camelCase)', 67, 'Renomeie para userName seguindo padrão camelCase', NOW() - INTERVAL 1 DAY);

COMMIT;
