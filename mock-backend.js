const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

// Middleware
app.use(cors());
app.use(express.json());

// Mock data
const mockModels = [
  'gemini-2.0-flash-001',
  'gemini-pro',
  'gpt-4-turbo',
  'gpt-3.5-turbo'
];

// Routes
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', timestamp: Date.now() });
});

app.get('/api/llm/models', (req, res) => {
  res.json({
    status: 'SUCCESS',
    message: 'Modelos LLM carregados com sucesso',
    models: mockModels,
    timestamp: Date.now()
  });
});

app.post('/api/llm/analyze', (req, res) => {
  const { code, fileName, model } = req.body;
  
  // Simulate processing time
  setTimeout(() => {
    const analysis = generateMockAnalysis(code, fileName, model);
    res.json({
      status: 'SUCCESS',
      message: 'Análise de código concluída',
      analysis: analysis,
      timestamp: Date.now()
    });
  }, 2000);
});

app.post('/api/llm/suggestions', (req, res) => {
  const { code, fileName, model } = req.body;
  
  setTimeout(() => {
    const suggestions = generateMockSuggestions(code, fileName);
    res.json({
      status: 'SUCCESS',
      message: 'Sugestões geradas com sucesso',
      suggestions: suggestions,
      timestamp: Date.now()
    });
  }, 1500);
});

app.post('/api/llm/review', (req, res) => {
  const { code, fileName, model, criteria } = req.body;
  
  setTimeout(() => {
    const review = generateMockCodeReview(code, fileName);
    res.json({
      status: 'SUCCESS',
      message: 'Code review concluído',
      review: review,
      timestamp: Date.now()
    });
  }, 3000);
});

// Mock generation functions
function generateMockAnalysis(code, fileName, model) {
  return `## Análise de Código - ${fileName}

**Modelo utilizado:** ${model}
**Arquivo:** ${fileName}

### Resumo da Análise
O código analisado apresenta várias oportunidades de melhoria, especialmente relacionadas aos padrões Sinqia de gerenciamento de conexões.

### Principais Achados

#### ? Pontos Positivos
- Estrutura de classes bem definida
- Uso adequado de imports
- Nomenclatura clara de variáveis

#### ?? Problemas Críticos Identificados
- **Vazamento de Conexão**: Detectado uso incorreto do padrão empresta()/devolve()
- **Segurança**: Senha armazenada em texto plano
- **Tratamento de Erro**: Retorno de null ao invés de exceptions

#### ?? Recomendações Sinqia
- Implementar padrão empresta()/devolve() em finally block
- Criptografar senhas antes do armazenamento
- Usar exceções específicas para tratamento de erros
- Adicionar paginação em consultas que retornam listas

### Pontuação Geral: 6.5/10

*Análise realizada com IA - ${model}*`;
}

function generateMockSuggestions(code, fileName) {
  return `## Sugestões de Melhoria - ${fileName}

### ?? Alta Prioridade
1. **Implementar padrão empresta()/devolve()** - Crítico para evitar vazamentos de conexão
2. **Criptografar senhas** - Usar BCrypt ou algoritmo similar
3. **Tratamento de exceções** - Lançar exceções específicas ao invés de retornar null

### ?? Média Prioridade
4. **Adicionar logging estruturado** - Facilitar debugging e auditoria
5. **Implementar validação de entrada** - Prevenir SQL injection
6. **Adicionar paginação** - Melhorar performance em consultas grandes

### ?? Baixa Prioridade
7. **Documentar métodos públicos** - Adicionar JavaDoc
8. **Refatorar métodos longos** - Melhorar legibilidade
9. **Implementar testes unitários** - Garantir qualidade

*Sugestões geradas com IA para conformidade com padrões Sinqia*`;
}

function generateMockCodeReview(code, fileName) {
  return `## Code Review - ${fileName}

### ?? Avaliação Geral
**Nota:** 6.5/10
**Status:** Necessita correções antes da aprovação

### ? Pontos Fortes
- Estrutura de código organizada
- Nomenclatura consistente
- Uso adequado de tipos Java

### ?? Problemas Identificados

#### Conformidade Sinqia
- ? **Padrão empresta()/devolve()**: Não implementado corretamente
- ? **Segurança**: Senha em texto plano
- ?? **Tratamento de erro**: Uso inadequado de return null

#### Qualidade do Código
- ?? **Validação**: Faltam validações de entrada
- ?? **Performance**: Ausência de paginação
- ?? **Documentação**: Métodos não documentados

### ?? Ações Obrigatórias
1. Corrigir padrão de gerenciamento de conexões
2. Implementar hash de senha
3. Adicionar tratamento adequado de exceções
4. Incluir validações de entrada

### ?? Checklist Sinqia
- ? Padrão empresta()/devolve() implementado
- ? Estrutura de pacotes adequada
- ? Tratamento de erro corporativo
- ?? Logging estruturado

**Recomendação:** Código reprovado. Implementar correções obrigatórias antes de nova revisão.

*Review automatizado com padrões Sinqia*`;
}

// Start server
app.listen(port, () => {
  console.log(`?? Mock Backend Server running at http://localhost:${port}`);
  console.log(`?? Available endpoints:`);
  console.log(`   GET  /api/health`);
  console.log(`   GET  /api/llm/models`);
  console.log(`   POST /api/llm/analyze`);
  console.log(`   POST /api/llm/suggestions`);
  console.log(`   POST /api/llm/review`);
});