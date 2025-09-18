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
      message: 'An�lise de c�digo conclu�da',
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
      message: 'Sugest�es geradas com sucesso',
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
      message: 'Code review conclu�do',
      review: review,
      timestamp: Date.now()
    });
  }, 3000);
});

// Mock generation functions
function generateMockAnalysis(code, fileName, model) {
  return `## An�lise de C�digo - ${fileName}

**Modelo utilizado:** ${model}
**Arquivo:** ${fileName}

### Resumo da An�lise
O c�digo analisado apresenta v�rias oportunidades de melhoria, especialmente relacionadas aos padr�es Sinqia de gerenciamento de conex�es.

### Principais Achados

#### ? Pontos Positivos
- Estrutura de classes bem definida
- Uso adequado de imports
- Nomenclatura clara de vari�veis

#### ?? Problemas Cr�ticos Identificados
- **Vazamento de Conex�o**: Detectado uso incorreto do padr�o empresta()/devolve()
- **Seguran�a**: Senha armazenada em texto plano
- **Tratamento de Erro**: Retorno de null ao inv�s de exceptions

#### ?? Recomenda��es Sinqia
- Implementar padr�o empresta()/devolve() em finally block
- Criptografar senhas antes do armazenamento
- Usar exce��es espec�ficas para tratamento de erros
- Adicionar pagina��o em consultas que retornam listas

### Pontua��o Geral: 6.5/10

*An�lise realizada com IA - ${model}*`;
}

function generateMockSuggestions(code, fileName) {
  return `## Sugest�es de Melhoria - ${fileName}

### ?? Alta Prioridade
1. **Implementar padr�o empresta()/devolve()** - Cr�tico para evitar vazamentos de conex�o
2. **Criptografar senhas** - Usar BCrypt ou algoritmo similar
3. **Tratamento de exce��es** - Lan�ar exce��es espec�ficas ao inv�s de retornar null

### ?? M�dia Prioridade
4. **Adicionar logging estruturado** - Facilitar debugging e auditoria
5. **Implementar valida��o de entrada** - Prevenir SQL injection
6. **Adicionar pagina��o** - Melhorar performance em consultas grandes

### ?? Baixa Prioridade
7. **Documentar m�todos p�blicos** - Adicionar JavaDoc
8. **Refatorar m�todos longos** - Melhorar legibilidade
9. **Implementar testes unit�rios** - Garantir qualidade

*Sugest�es geradas com IA para conformidade com padr�es Sinqia*`;
}

function generateMockCodeReview(code, fileName) {
  return `## Code Review - ${fileName}

### ?? Avalia��o Geral
**Nota:** 6.5/10
**Status:** Necessita corre��es antes da aprova��o

### ? Pontos Fortes
- Estrutura de c�digo organizada
- Nomenclatura consistente
- Uso adequado de tipos Java

### ?? Problemas Identificados

#### Conformidade Sinqia
- ? **Padr�o empresta()/devolve()**: N�o implementado corretamente
- ? **Seguran�a**: Senha em texto plano
- ?? **Tratamento de erro**: Uso inadequado de return null

#### Qualidade do C�digo
- ?? **Valida��o**: Faltam valida��es de entrada
- ?? **Performance**: Aus�ncia de pagina��o
- ?? **Documenta��o**: M�todos n�o documentados

### ?? A��es Obrigat�rias
1. Corrigir padr�o de gerenciamento de conex�es
2. Implementar hash de senha
3. Adicionar tratamento adequado de exce��es
4. Incluir valida��es de entrada

### ?? Checklist Sinqia
- ? Padr�o empresta()/devolve() implementado
- ? Estrutura de pacotes adequada
- ? Tratamento de erro corporativo
- ?? Logging estruturado

**Recomenda��o:** C�digo reprovado. Implementar corre��es obrigat�rias antes de nova revis�o.

*Review automatizado com padr�es Sinqia*`;
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