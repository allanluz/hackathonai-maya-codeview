-- MAYA Code Review System - Dados Iniciais
-- Configura��es padr�o e dados de exemplo para desenvolvimento

-- ===================================================================
-- CONFIGURA��ES DO SISTEMA
-- ===================================================================

-- Configura��es TFS
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('tfs.server.url', 'https://tfs.sinqia.com.br', 'STRING', 'TFS', 'URL do servidor TFS/Azure DevOps', false, true),
('tfs.collection', 'GestaoRecursos', 'STRING', 'TFS', 'Collection do TFS', false, true),
('tfs.username', 'allan.luz', 'STRING', 'TFS', 'Usu�rio para autentica��o TFS', false, true),
('tfs.timeout.connection', '30000', 'INTEGER', 'TFS', 'Timeout de conex�o TFS em milissegundos', false, false),
('tfs.timeout.read', '60000', 'INTEGER', 'TFS', 'Timeout de leitura TFS em milissegundos', false, false);

-- Configura��es MAYA Analysis
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.analysis.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar an�lise MAYA', false, true),
('maya.analysis.connection-leak.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar detec��o de vazamento de conex�o', false, false),
('maya.analysis.complexity.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar an�lise de complexidade', false, false),
('maya.analysis.security.enabled', 'true', 'BOOLEAN', 'ANALYSIS', 'Habilitar an�lise de seguran�a', false, false),
('maya.analysis.complexity.threshold.high', '15', 'INTEGER', 'ANALYSIS', 'Limite para complexidade alta', false, false),
('maya.analysis.complexity.threshold.critical', '20', 'INTEGER', 'ANALYSIS', 'Limite para complexidade cr�tica', false, false);

-- Configura��es Sinqia AI
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.ai.enabled', 'true', 'BOOLEAN', 'AI', 'Habilitar integra��o com IA da Sinqia', false, false),
('maya.ai.base-url', 'http://everai.sinqia.com.br', 'URL', 'AI', 'URL base da API Sinqia AI', false, false),
('maya.ai.timeout', '30000', 'INTEGER', 'AI', 'Timeout para chamadas de IA em milissegundos', false, false),
('maya.ai.default-model', 'gpt-4', 'STRING', 'AI', 'Modelo LLM padr�o para an�lise', false, false);

-- Configura��es de Severidade
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.severity.connection-leak.missing-devolve', 'CRITICAL', 'STRING', 'SEVERITY', 'Severidade para empresta() sem devolve()', false, false),
('maya.severity.connection-leak.not-in-finally', 'ERROR', 'STRING', 'SEVERITY', 'Severidade para devolve() fora do finally', false, false),
('maya.severity.complexity.high', 'WARNING', 'STRING', 'SEVERITY', 'Severidade para alta complexidade', false, false),
('maya.severity.security.sql-injection', 'CRITICAL', 'STRING', 'SEVERITY', 'Severidade para SQL injection', false, false);

-- Configura��es de Performance
INSERT INTO configuration_settings (key_name, value, data_type, category, description, is_sensitive, is_required) VALUES
('maya.performance.parallel-processing', 'true', 'BOOLEAN', 'PERFORMANCE', 'Habilitar processamento paralelo', false, false),
('maya.performance.max-threads', '10', 'INTEGER', 'PERFORMANCE', 'N�mero m�ximo de threads para an�lise', false, false),
('maya.performance.batch-size', '50', 'INTEGER', 'PERFORMANCE', 'Tamanho do lote para processamento', false, false),
('maya.performance.timeout.analysis', '300000', 'INTEGER', 'PERFORMANCE', 'Timeout para an�lise completa em milissegundos', false, false);

-- ===================================================================
-- ARQUIVOS AUXILIARES
-- ===================================================================

-- Documenta��o de padr�es Sinqia
INSERT INTO auxiliary_files (name, content, file_type, description, is_active, version, tags) VALUES
('padroes-sinqia.md', 
'# Padr�es de Desenvolvimento Sinqia

## ?? Gest�o de Conex�es (CR�TICO)

### Regra Fundamental
**TODA** chamada `empresta()` DEVE ter `devolve()` correspondente em bloco `finally`.

### ? Exemplo Correto
```java
Connection conn = null;
try {
    conn = ConexaoFactory.empresta();
    // usar conex�o
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
    // usar conex�o
} catch (Exception e) {
    // se houver exce��o, devolve() n�o ser� executado
}
ConexaoFactory.devolve(conn); // VAZAMENTO!
```

## ?? Nomenclatura

### Classes
- **PascalCase**: `UsuarioService`, `RelatorioController`
- **Nomes em portugu�s**: Usar termos do dom�nio da aplica��o
- **Sufixos obrigat�rios**:
  - Services: `*Service`
  - Controllers: `*Controller`
  - Repositories: `*Repository` ou `*Dao`

### M�todos e Vari�veis
- **camelCase**: `buscarUsuario()`, `nomeCompleto`
- **Verbos para m�todos**: `salvar()`, `buscar()`, `validar()`
- **Substantivos para vari�veis**: `usuario`, `listaRelatorios`

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

### Camadas Obrigat�rias
- `dao` - Acesso a dados
- `dto` - Objetos de transfer�ncia
- `service` - L�gica de neg�cio
- `controller` - Controladores web
- `util` - Utilit�rios

## ?? Seguran�a

### SQL Injection
```java
// ? CORRETO - PreparedStatement
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuario WHERE login = ?");
stmt.setString(1, login);

// ? INCORRETO - Concatena��o
String sql = "SELECT * FROM usuario WHERE login = ''" + login + "''"; // VULNER�VEL!
```

### Logs Seguros
```java
// ? CORRETO
logger.info("Login realizado para usu�rio: {}", usuario.getId());

// ? INCORRETO
logger.info("Login: " + login + ", Senha: " + senha); // VAZA SENHA!
```

## ?? M�tricas de Qualidade

### Complexidade Ciclom�tica
- **Baixa**: 1-5 (�timo)
- **M�dia**: 6-10 (Aceit�vel)
- **Alta**: 11-15 (Revisar)
- **Cr�tica**: >15 (Refatorar obrigat�rio)

### Tamanho de M�todos
- **M�ximo**: 50 linhas
- **Recomendado**: 20 linhas
- **Ideal**: 10 linhas

### Tamanho de Classes
- **M�ximo**: 500 linhas
- **Recomendado**: 200 linhas

## ?? Boas Pr�ticas

### 1. Tratamento de Exce��es
```java
try {
    // c�digo que pode falhar
} catch (SpecificException e) {
    logger.error("Erro espec�fico: {}", e.getMessage());
    throw new BusinessException("Mensagem amig�vel", e);
} catch (Exception e) {
    logger.error("Erro inesperado", e);
    throw new SystemException("Erro interno", e);
}
```

### 2. Valida��es
```java
public void salvarUsuario(Usuario usuario) {
    if (usuario == null) {
        throw new IllegalArgumentException("Usu�rio n�o pode ser nulo");
    }
    if (StringUtils.isBlank(usuario.getNome())) {
        throw new ValidationException("Nome � obrigat�rio");
    }
    // continuar valida��es
}
```

### 3. Documenta��o
```java
/**
 * Busca usu�rio ativo por ID.
 * 
 * @param id ID do usu�rio (obrigat�rio)
 * @return Usuario encontrado
 * @throws UsuarioNotFoundException se n�o encontrado
 * @throws IllegalArgumentException se ID inv�lido
 */
public Usuario buscarPorId(Long id) {
    // implementa��o
}
```

## ?? Checklist de Code Review

### ? Obrigat�rio
- [ ] Licen�a Sinqia no cabe�alho
- [ ] Conex�es balanceadas (empresta/devolve)
- [ ] Nomenclatura em portugu�s
- [ ] Estrutura de pacotes correta
- [ ] Tratamento de exce��es
- [ ] Logs n�o sens�veis
- [ ] PreparedStatement para SQL
- [ ] Javadoc em m�todos p�blicos

### ?? Recomendado
- [ ] Complexidade baixa (<10)
- [ ] M�todos pequenos (<20 linhas)
- [ ] Classes focadas (responsabilidade �nica)
- [ ] Testes unit�rios
- [ ] Valida��o de par�metros
- [ ] Constantes para strings m�gicas
',
'DOCUMENTATION', 
'Padr�es de desenvolvimento e code review da Sinqia', 
true, 
'1.0', 
'sinqia,padroes,desenvolvimento,conexao,qualidade');

-- Prompt padr�o para an�lise MAYA
INSERT INTO auxiliary_files (name, content, file_type, description, is_active, version, tags) VALUES
('maya-analysis-prompt.txt',
'Voc� � um especialista em revis�o de c�digo focado nos padr�es MAYA da Sinqia.

INSTRU��ES DE AN�LISE:

1. GEST�O DE CONEX�ES (PRIORIDADE M�XIMA):
   - Verificar empresta()/devolve() balanceados
   - Identificar vazamentos de conex�o
   - Validar uso de try-finally
   - Detectar return/throw antes de devolve()

2. PADR�ES SINQIA:
   - Nomenclatura em portugu�s
   - Estrutura de pacotes com.sinqia.*
   - Suffixos obrigat�rios (Service, Controller, etc)
   - Licen�a no cabe�alho

3. QUALIDADE DE C�DIGO:
   - Complexidade ciclom�tica
   - Tamanho de m�todos e classes
   - C�digo duplicado
   - Padr�es de formata��o

4. SEGURAN�A:
   - SQL injection (concatena��o vs PreparedStatement)
   - Dados sens�veis em logs
   - Tratamento adequado de exce��es
   - Valida��o de entrada

5. ARQUITETURA:
   - Separa��o de responsabilidades
   - Uso correto de camadas
   - Depend�ncias apropriadas

FORMATO DE RESPOSTA:
Retorne an�lise em formato JSON:
{
  "issues": [
    {
      "severity": "CRITICAL|ERROR|WARNING|INFO",
      "type": "CONNECTION_LEAK|SECURITY_ISSUE|CODE_QUALITY|etc",
      "title": "T�tulo conciso",
      "description": "Descri��o detalhada",
      "lineNumber": 123,
      "suggestion": "Como corrigir"
    }
  ]
}

CRIT�RIOS DE SEVERIDADE:
- CRITICAL: Vazamentos de conex�o, SQL injection
- ERROR: Viola��es graves de padr�o, bugs evidentes
- WARNING: Complexidade alta, m�s pr�ticas
- INFO: Sugest�es de melhoria, otimiza��es',
'PROMPT',
'Prompt padr�o para an�lise MAYA com IA',
true,
'1.0',
'maya,prompt,ia,analise');

-- Template de relat�rio
INSERT INTO auxiliary_files (name, content, file_type, description, is_active, version, tags) VALUES
('relatorio-executivo.md',
'# Relat�rio Executivo MAYA - {{repositorio}}

**Per�odo:** {{dataInicio}} - {{dataFim}}
**Gerado em:** {{dataGeracao}}

## ?? Resumo Executivo

- **Total de arquivos analisados:** {{totalArquivos}}
- **Issues encontrados:** {{totalIssues}}
- **Issues cr�ticos:** {{issuesCriticos}}
- **Score m�dio de qualidade:** {{scoreMedia}}/100

## ?? Issues Cr�ticos

{{#issuesCriticos}}
### {{titulo}}
- **Arquivo:** `{{arquivo}}`
- **Linha:** {{linha}}
- **Descri��o:** {{descricao}}
- **Impacto:** {{impacto}}
- **Recomenda��o:** {{recomendacao}}

{{/issuesCriticos}}

## ?? M�tricas de Qualidade

### Distribui��o de Issues
| Severidade | Quantidade | Percentual |
|------------|------------|------------|
| Cr�tico    | {{criticos}} | {{percentualCriticos}}% |
| Alto       | {{altos}} | {{percentualAltos}}% |
| M�dio      | {{medios}} | {{percentualMedios}}% |
| Baixo      | {{baixos}} | {{percentualBaixos}}% |

### Top 5 Tipos de Issues
{{#topIssues}}
1. **{{tipo}}:** {{quantidade}} ocorr�ncias
{{/topIssues}}

### Arquivos com Mais Issues
{{#arquivosProblematicos}}
1. **{{arquivo}}:** {{issues}} issues ({{criticos}} cr�ticos)
{{/arquivosProblematicos}}

## ?? An�lise de Conex�es

- **Arquivos com vazamentos:** {{arquivosComVazamentos}}
- **Total de vazamentos detectados:** {{totalVazamentos}}
- **Taxa de conformidade:** {{taxaConformidade}}%

### Detalhes dos Vazamentos
{{#vazamentos}}
- **{{arquivo}}** (linha {{linha}}): {{descricao}}
{{/vazamentos}}

## ??? Conformidade Arquitetural

- **Padr�es Sinqia:** {{conformidadePadroes}}%
- **Estrutura de pacotes:** {{conformidadePacotes}}%
- **Nomenclatura:** {{conformidadeNomes}}%

## ?? Recomenda��es

### A��es Imediatas (Cr�ticas)
{{#acoesCriticas}}
- {{acao}}
{{/acoesCriticas}}

### Melhorias de M�dio Prazo
{{#melhorias}}
- {{melhoria}}
{{/melhorias}}

### Sugest�es de Longo Prazo
{{#sugestoes}}
- {{sugestao}}
{{/sugestoes}}

## ?? Checklist de Qualidade

- [ ] Todos os vazamentos de conex�o corrigidos
- [ ] Issues cr�ticos resolvidos
- [ ] Complexidade ciclom�tica dentro dos limites
- [ ] Padr�es de nomenclatura seguidos
- [ ] Documenta��o adequada
- [ ] Testes unit�rios criados/atualizados

---
*Relat�rio gerado automaticamente pelo Sistema MAYA v{{versaoMaya}}*',
'TEMPLATE',
'Template para gera��o de relat�rios executivos',
true,
'1.0',
'relatorio,template,executivo');

-- ===================================================================
-- DADOS DE EXEMPLO PARA DESENVOLVIMENTO
-- ===================================================================

-- Code Review de exemplo
INSERT INTO code_reviews (commit_sha, repository_name, project_name, author, title, status, critical_issues, total_issues, analysis_score, created_at) VALUES
('abc123def456', 'sistema-exemplo', 'Projeto Exemplo', 'jo�o.silva', 'Implementa��o inicial do servi�o de usu�rios', 'COMPLETED', 2, 8, 75.5, NOW() - INTERVAL 1 DAY),
('def456ghi789', 'sistema-exemplo', 'Projeto Exemplo', 'maria.santos', 'Corre��o de vazamentos de conex�o', 'COMPLETED', 0, 3, 92.0, NOW() - INTERVAL 2 HOUR),
('ghi789jkl012', 'api-financeiro', 'Sistema Financeiro', 'pedro.costa', 'Nova funcionalidade de relat�rios', 'PENDING', 1, 5, 68.2, NOW() - INTERVAL 30 MINUTE);

-- File Analysis de exemplo
INSERT INTO file_analyses (code_review_id, file_path, class_name, language, line_count, complexity_score, connection_imbalance, score, connection_empresta, connection_devolve, connection_balanced, created_at) VALUES
(1, '/src/main/java/com/sinqia/exemplo/service/UsuarioService.java', 'UsuarioService', 'java', 145, 8.5, 0.0, 85.0, 3, 3, true, NOW() - INTERVAL 1 DAY),
(1, '/src/main/java/com/sinqia/exemplo/dao/UsuarioDao.java', 'UsuarioDao', 'java', 89, 12.0, 33.3, 65.0, 2, 1, false, NOW() - INTERVAL 1 DAY),
(2, '/src/main/java/com/sinqia/exemplo/dao/UsuarioDao.java', 'UsuarioDao', 'java', 92, 11.5, 0.0, 88.0, 2, 2, true, NOW() - INTERVAL 2 HOUR);

-- Analysis Issues de exemplo
INSERT INTO analysis_issues (file_analysis_id, severity, type, title, description, line_number, suggestion, created_at) VALUES
(1, 'WARNING', 'COMPLEXITY', 'Alta complexidade ciclom�tica', 'M�todo buscarUsuarios() possui complexidade de 12 (limite: 10)', 45, 'Considere dividir o m�todo em fun��es menores', NOW() - INTERVAL 1 DAY),
(2, 'CRITICAL', 'CONNECTION_LEAK', 'Vazamento de conex�o detectado', 'empresta() na linha 23 sem devolve() correspondente', 23, 'Adicione devolve() em bloco finally', NOW() - INTERVAL 1 DAY),
(2, 'ERROR', 'STYLE_VIOLATION', 'Viola��o de padr�o de nomenclatura', 'Vari�vel user_name deveria ser userName (camelCase)', 67, 'Renomeie para userName seguindo padr�o camelCase', NOW() - INTERVAL 1 DAY);

COMMIT;
