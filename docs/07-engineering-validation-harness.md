# Engineering Validation Harness

# DevReport

Versão: 1.0

Data: Julho/2026

Autor: Thiago Carlos Andrade

---

## 1. Objetivo

Este documento define o Engineering Validation Harness do DevReport.

O harness é um protocolo obrigatório de validação executado após a implementação de cada task. Ele garante que cada entrega seja verificada contra Tasks, Specifications, Features, Product Backlog, PRD e TDD antes que a próxima task seja iniciada.

---

## 2. Decisão de Workflow

O DevReport usará validação bloqueante após cada task.

```text
Implementar TASK-N
        ↓
Executar validação da TASK-N
        ↓
Registrar resultado PASS/FAIL
        ↓
PASS: liberar próxima task
FAIL: corrigir antes de avançar
```

---

## 3. Regras do Harness

| Regra | Decisão |
|---|---|
| Execução | Manual/agent-driven |
| Gate | Bloqueante |
| Frequência | Após cada task implementada |
| Integrações externas em testes | Mockadas ou simuladas |
| GitHub real | Não obrigatório para validação automatizada |
| OpenAI real | Não obrigatório para validação automatizada |
| Avanço para próxima task | Permitido somente após PASS |

---

## 4. Fontes de Verdade

O harness deve validar a implementação contra estes documentos:

| Documento | Uso no harness |
|---|---|
| `docs/00-project-brief.md` | Objetivo, escopo e restrições do MVP |
| `docs/01-product-requirements-document.md` | Requisitos funcionais, não funcionais, regras e fluxos alternativos |
| `docs/02-technical-design-document.md` | Arquitetura, camadas, serviços, fluxo LangGraph4j e decisões técnicas |
| `docs/03-product-backlog.md` | PBIs, prioridades e critérios de aceite |
| `docs/04-features.md` | Features e rastreabilidade com requisitos |
| `docs/05-specifications.md` | Comportamento implementável, contratos e regras |
| `docs/06-tasks.md` | Tasks, dependências e verificação esperada |
| `docs/08-task-validation-matrix.md` | Checks obrigatórios por task |

---

## 5. Responsabilidade do Agente

Após implementar uma task, o agente deve:

1. Identificar a task implementada.
2. Ler a entrada correspondente em `docs/08-task-validation-matrix.md`.
3. Executar os comandos aplicáveis diretamente no terminal.
4. Inspecionar os arquivos alterados.
5. Validar critérios funcionais, técnicos e arquiteturais.
6. Confirmar rastreabilidade com a Specification correspondente.
7. Reportar `PASS` ou `FAIL`.
8. Em caso de `FAIL`, corrigir a implementação antes de avançar.

O agente não deve implementar a próxima task enquanto o gate da task atual estiver falhando.

---

## 6. Níveis de Validação

### 6.1 Validação de Build

Confirma que a base continua compilável.

Checks típicos:

- `mvn compile`
- `mvn test`
- ausência de erro de dependência;
- ausência de erro de configuração quebrando o contexto da aplicação.

### 6.2 Validação Funcional

Confirma que a task entrega o comportamento esperado.

Exemplos:

- período inválido é rejeitado;
- issues fora do período são descartadas;
- métricas são calculadas corretamente;
- erro de IA não bloqueia dashboard;
- estado sem entregas é exibido.

### 6.3 Validação Arquitetural

Confirma aderência ao TDD e às decisões de arquitetura.

Checks obrigatórios:

- domínio não depende de Spring;
- controller não contém regra de negócio;
- integração GitHub não calcula métricas;
- cálculo de métricas não chama IA;
- IA é opcional e não bloqueante;
- LangGraph4j coordena o fluxo principal quando as tasks do agente forem implementadas.

### 6.4 Validação de Segurança

Confirma que credenciais e dados sensíveis não foram expostos.

Checks obrigatórios:

- token GitHub não aparece em logs;
- token GitHub não aparece em templates;
- credenciais OpenAI não aparecem em logs;
- mensagens de erro não exibem stack trace ao usuário;
- arquivos versionados não devem conter segredos reais.

### 6.5 Validação de Rastreabilidade

Confirma que a implementação da task corresponde à documentação.

Para cada task, verificar:

- Task implementada em `docs/06-tasks.md`;
- Specification relacionada em `docs/05-specifications.md`;
- Feature relacionada em `docs/04-features.md`;
- PBI relacionado em `docs/03-product-backlog.md`;
- requisito funcional, não funcional ou regra de negócio quando aplicável.

---

## 7. Gate Bloqueante

Cada task recebe um status de validação.

| Status | Significado | Próxima task pode iniciar? |
|---|---|---|
| PASS | Todos os checks obrigatórios passaram | Sim |
| FAIL | Um ou mais checks obrigatórios falharam | Não |
| BLOCKED | Validação não pôde ser executada por impedimento externo ou falta de informação | Não |
| NOT APPLICABLE | Check não se aplica à task específica | Sim, se demais checks passarem |

Uma task só pode ser considerada concluída se o resultado final for `PASS`.

---

## 8. Formato de Relatório Após Cada Task

Após a validação, o agente deve reportar no chat:

```text
Engineering Validation Report

Task: TASK-016 - Implementar cálculo de métricas consolidadas
Status: PASS

Checks executados:
- [PASS] mvn compile
- [PASS] mvn test -Dtest=MetricsServiceTest
- [PASS] Total corresponde à soma das categorias
- [PASS] Lista vazia retorna métricas zeradas
- [PASS] Regra de negócio não está no controller
- [PASS] Rastreabilidade com SPEC-004 confirmada

Evidências:
- MetricsServiceTest passou
- Arquivo MetricsService implementa cálculo isolado
- Nenhuma dependência de IA ou controller no cálculo

Próxima task liberada: Sim
```

Em caso de falha:

```text
Engineering Validation Report

Task: TASK-016 - Implementar cálculo de métricas consolidadas
Status: FAIL

Checks executados:
- [PASS] mvn compile
- [FAIL] mvn test -Dtest=MetricsServiceTest
- [FAIL] Lista vazia não retorna métricas zeradas

Ação necessária:
- Corrigir MetricsService para retornar Metric com zeros quando a lista de issues estiver vazia.

Próxima task liberada: Não
```

---

## 9. Checks Globais Após Cada Task

Estes checks devem ser considerados em toda validação, ajustando conforme a maturidade do projeto:

| Check | Obrigatório | Observação |
|---|---|---|
| Compilar projeto | Sim | `mvn compile`, quando o Maven estiver disponível |
| Rodar testes existentes | Sim | `mvn test`, quando já houver testes |
| Verificar task atual | Sim | Conforme `docs/08-task-validation-matrix.md` |
| Verificar arquitetura | Sim | Principalmente separação controller/domain/infrastructure |
| Verificar segredos | Sim | Nenhum token real em logs, templates ou arquivos versionados |
| Verificar rastreabilidade | Sim | Task deve cumprir a SPEC relacionada |
| Verificar regressão óbvia | Sim | Mudanças não devem quebrar fluxo já implementado |

Se o projeto ainda não tiver Maven, testes ou estrutura suficiente para rodar um check, o agente deve registrar o check como `NOT APPLICABLE` e explicar o motivo.

---

## 10. Política para Dependências Externas

Durante validação automática por task:

- GitHub deve ser mockado, simulado ou isolado em testes.
- OpenAI deve ser mockada, simulada ou isolada em testes.
- Chamadas reais a APIs externas não são obrigatórias para liberar task.
- Testes não devem depender de internet.
- Testes não devem depender de credenciais reais.

Validações com APIs reais podem existir como verificação manual complementar, mas não devem ser gate obrigatório do desenvolvimento diário do MVP.

---

## 11. Política para Falhas

Quando uma task falhar no harness:

1. Não iniciar a próxima task.
2. Identificar o check que falhou.
3. Corrigir a implementação.
4. Reexecutar os checks da task.
5. Reexecutar checks globais aplicáveis.
6. Reportar novo resultado.

Se a falha revelar problema na documentação, o agente deve parar e pedir realinhamento antes de alterar o escopo.

---

## 12. Definition of Done do Harness

O harness estará corretamente aplicado quando:

- cada task implementada tiver relatório `PASS`;
- nenhuma task seguinte for iniciada após `FAIL`;
- os checks da matriz forem executados ou justificados;
- build e testes forem executados quando disponíveis;
- regras arquiteturais forem verificadas;
- segredos não forem expostos;
- a rastreabilidade task/specification for confirmada;
- integrações externas forem mockadas nos testes automatizados.

