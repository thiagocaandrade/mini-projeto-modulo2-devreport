# Task Validation Matrix

# DevReport

Versão: 1.0

Data: Julho/2026

Autor: Thiago Carlos Andrade

---

## 1. Objetivo

Esta matriz define os checks obrigatórios do Engineering Validation Harness para cada task do DevReport.

Após implementar uma task, o agente deve executar os checks correspondentes nesta matriz, além dos checks globais definidos em `docs/07-engineering-validation-harness.md`.

---

## 2. Checks Globais

Aplicar após cada task, quando tecnicamente possível:

| Check | Comando ou ação |
|---|---|
| Build | `mvn compile` |
| Testes existentes | `mvn test` |
| Rastreabilidade | Confirmar task em `docs/06-tasks.md` e SPEC em `docs/05-specifications.md` |
| Arquitetura | Inspecionar se regras ficaram fora de controllers e infraestrutura externa |
| Segurança | Verificar ausência de tokens reais em código, templates, logs e documentação |
| Regressão | Confirmar que comportamento já implementado não foi quebrado |

Se algum check não se aplicar no momento da task, registrar como `NOT APPLICABLE` com justificativa.

---

## 3. Matriz por Task

### TASK-001 - Criar projeto Spring Boot com Maven

**Specification:** SPEC-001

**Checks obrigatórios:**

- Confirmar existência de `pom.xml`.
- Confirmar existência da classe principal Spring Boot.
- Executar `mvn compile`, se Maven estiver disponível.
- Confirmar que a estrutura criada não inclui banco, login ou funcionalidades fora do MVP.

**Gate:** Bloqueante.

---

### TASK-002 - Configurar dependências principais

**Specification:** SPEC-001

**Checks obrigatórios:**

- Executar `mvn dependency:resolve`, se disponível.
- Executar `mvn compile`.
- Confirmar dependências de Spring Web, Thymeleaf, validação, testes, Spring AI e LangGraph4j conforme aplicável.
- Confirmar que dependências não introduzem banco de dados obrigatório.

**Gate:** Bloqueante.

---

### TASK-003 - Criar estrutura de pacotes

**Specification:** SPEC-001

**Checks obrigatórios:**

- Confirmar pacotes em `src/main/java/br/com/devreport`.
- Confirmar existência dos pacotes `config`, `controller`, `application`, `domain`, `infrastructure`, `github`, `metrics`, `dashboard`, `ai`, `agent` e `shared`.
- Confirmar que não há regra de negócio em `controller`.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-004 - Criar modelos de domínio

**Specification:** SPEC-001

**Checks obrigatórios:**

- Confirmar modelos `AnalysisRequest`, `Issue`, `Metric`, `DashboardReport` e `Insight`.
- Confirmar campos mínimos definidos em `docs/05-specifications.md`.
- Confirmar que domínio não importa Spring.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-005 - Criar DTOs principais

**Specification:** SPEC-001

**Checks obrigatórios:**

- Confirmar DTOs `AnalysisRequestDTO`, `IssueDTO`, `MetricDTO`, `DashboardDTO` e `InsightDTO`.
- Confirmar que DTOs não contêm regra de negócio.
- Confirmar que DTOs são adequados para controller/view.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-006 - Configurar propriedades do GitHub

**Specification:** SPEC-002

**Checks obrigatórios:**

- Confirmar propriedades `github.token`, `github.owner`, `github.repository` e `github.project` quando aplicável.
- Confirmar classe de configuração ou mecanismo equivalente.
- Confirmar que nenhum token real foi versionado.
- Confirmar que token não é logado.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-007 - Criar tela/formulário de período

**Specification:** SPEC-002

**Checks obrigatórios:**

- Confirmar campos `startDate` e `endDate`.
- Confirmar action/método de envio para o backend.
- Confirmar que a tela renderiza via Thymeleaf.
- Confirmar layout básico responsivo.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-008 - Validar entrada de período

**Specification:** SPEC-002

**Checks obrigatórios:**

- Validar datas obrigatórias.
- Validar que `endDate >= startDate`.
- Confirmar que período inválido não consulta GitHub.
- Criar ou executar teste de validação, quando estrutura de testes existir.
- Executar `mvn test`, se houver testes aplicáveis.

**Gate:** Bloqueante.

---

### TASK-009 - Criar controller do dashboard

**Specification:** SPEC-002

**Checks obrigatórios:**

- Confirmar endpoint `GET` para tela inicial.
- Confirmar endpoint `POST` ou equivalente para solicitação de análise.
- Confirmar que controller delega regra de negócio.
- Confirmar que controller não acessa GitHub diretamente.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-010 - Implementar cliente HTTP do GitHub

**Specification:** SPEC-003

**Checks obrigatórios:**

- Confirmar uso das propriedades GitHub configuradas.
- Confirmar envio de autenticação sem logar token.
- Confirmar isolamento em infraestrutura/adapters.
- Confirmar que cliente não calcula métricas.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-011 - Implementar consulta de issues concluídas

**Specification:** SPEC-003

**Checks obrigatórios:**

- Confirmar consulta de issues fechadas/concluídas.
- Confirmar que issues abertas não entram no resultado.
- Confirmar tratamento inicial de erro HTTP.
- Preferir teste com mock da API.
- Executar `mvn test`, se teste existir.

**Gate:** Bloqueante.

---

### TASK-012 - Implementar mapper de GitHub para domínio

**Specification:** SPEC-003

**Checks obrigatórios:**

- Confirmar mapeamento de id, title, description, labels, closedAt, author e project quando disponível.
- Confirmar tolerância a campos opcionais ausentes.
- Confirmar que labels são preservadas.
- Executar teste de mapper, se existir.
- Executar `mvn test`.

**Gate:** Bloqueante.

---

### TASK-013 - Implementar filtro por período

**Specification:** SPEC-003

**Checks obrigatórios:**

- Confirmar inclusão de issues com `closedAt` dentro do período.
- Confirmar exclusão de issues antes do período.
- Confirmar exclusão de issues depois do período.
- Confirmar exclusão de issues sem `closedAt`.
- Executar teste unitário do filtro, se existir.

**Gate:** Bloqueante.

---

### TASK-014 - Tratar erros básicos da integração GitHub

**Specification:** SPEC-003

**Checks obrigatórios:**

- Confirmar tratamento para 401/403.
- Confirmar tratamento para erro de rede ou indisponibilidade.
- Confirmar que token não aparece em mensagem ou log.
- Confirmar conversão para erro amigável.
- Executar testes de erro com GitHub mockado.

**Gate:** Bloqueante.

---

### TASK-015 - Implementar classificador de issues

**Specification:** SPEC-004

**Checks obrigatórios:**

- Confirmar classificação Feature.
- Confirmar classificação Bug.
- Confirmar classificação Task.
- Confirmar fallback para Task quando label ausente ou desconhecida.
- Confirmar que cada issue recebe apenas uma categoria.
- Executar teste unitário de classificação.

**Gate:** Bloqueante.

---

### TASK-016 - Implementar cálculo de métricas consolidadas

**Specification:** SPEC-004

**Checks obrigatórios:**

- Confirmar cálculo de total.
- Confirmar cálculo de features, bugs e tasks.
- Confirmar que total equivale à soma das categorias.
- Confirmar lista vazia retornando zeros.
- Confirmar que cálculo não chama IA nem controller.
- Executar teste unitário de métricas.

**Gate:** Bloqueante.

---

### TASK-017 - Implementar dados de gráfico por período

**Specification:** SPEC-004

**Checks obrigatórios:**

- Confirmar agregação de entregas por período.
- Confirmar saída com labels e valores.
- Confirmar estrutura serializável para Thymeleaf/Chart.js.
- Confirmar lista vazia com saída controlada.
- Executar teste unitário quando disponível.

**Gate:** Bloqueante.

---

### TASK-018 - Implementar dados de gráfico por categoria

**Specification:** SPEC-004

**Checks obrigatórios:**

- Confirmar categorias Feature, Bug e Task.
- Confirmar valores iguais aos de `Metric`.
- Confirmar saída compatível com Chart.js.
- Confirmar zeros para lista vazia.
- Executar teste unitário quando disponível.

**Gate:** Bloqueante.

---

### TASK-019 - Criar estado AnalysisState

**Specification:** SPEC-005

**Checks obrigatórios:**

- Confirmar campos `startDate`, `endDate`, `issues`, `metrics`, `summary`, `dashboard` e `errors`.
- Confirmar que estado pode ser atualizado por nodes.
- Confirmar que estado não acopla domínio à infraestrutura externa.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-020 - Implementar nodes do LangGraph4j

**Specification:** SPEC-005

**Checks obrigatórios:**

- Confirmar nodes `StartNode`, `ValidateRequestNode`, `FetchGitHubDataNode`, `CalculateMetricsNode`, `GenerateInsightsNode` e `BuildDashboardNode`.
- Confirmar responsabilidade única por node.
- Confirmar atualização explícita de `AnalysisState`.
- Confirmar que validação inválida não chama GitHub.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-021 - Montar fluxo principal do agente

**Specification:** SPEC-005

**Checks obrigatórios:**

- Confirmar ordem: validação, GitHub, métricas, IA, dashboard.
- Confirmar que métricas executam antes da IA.
- Confirmar que erro da IA não bloqueia dashboard.
- Confirmar que erro do GitHub gera caminho de erro amigável.
- Executar teste de fluxo com mocks, se disponível.

**Gate:** Bloqueante.

---

### TASK-022 - Integrar controller com agente

**Specification:** SPEC-005

**Checks obrigatórios:**

- Confirmar que controller aciona agente.
- Confirmar que controller recebe `DashboardReport`.
- Confirmar que controller envia resultado para view.
- Confirmar que controller não contém fluxo manual duplicado.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-023 - Criar serviço de montagem do DashboardReport

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar consolidação de métricas, gráficos, resumo e mensagem.
- Confirmar suporte a resumo opcional.
- Confirmar suporte a estado vazio ou erro.
- Confirmar que serviço não consulta GitHub nem IA diretamente.
- Executar teste unitário quando disponível.

**Gate:** Bloqueante.

---

### TASK-024 - Criar layout base Thymeleaf

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar template Thymeleaf.
- Confirmar Bootstrap 5.
- Confirmar presença de formulário e área de relatório.
- Confirmar ausência de texto de stack trace ou segredo.
- Executar aplicação ou teste de renderização quando disponível.

**Gate:** Bloqueante.

---

### TASK-025 - Implementar cards de métricas

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar card Total de Entregas.
- Confirmar card Features.
- Confirmar card Bugs.
- Confirmar card Tasks.
- Confirmar valores vindos do backend.
- Confirmar que zeros são exibidos corretamente.

**Gate:** Bloqueante.

---

### TASK-026 - Integrar Chart.js

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar carregamento do Chart.js.
- Confirmar local de injeção dos dados dos gráficos.
- Confirmar ausência de erro JavaScript óbvio no template.
- Confirmar que integração não depende de API externa do backend.

**Gate:** Bloqueante.

---

### TASK-027 - Renderizar gráfico de entregas por período

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar uso dos dados gerados na TASK-017.
- Confirmar labels e valores corretos.
- Confirmar comportamento com lista vazia.
- Confirmar que gráfico não conflita com cards.

**Gate:** Bloqueante.

---

### TASK-028 - Renderizar gráfico de distribuição por categoria

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar uso dos dados gerados na TASK-018.
- Confirmar categorias Feature, Bug e Task.
- Confirmar valores iguais aos cards/métricas.
- Confirmar comportamento com zeros.

**Gate:** Bloqueante.

---

### TASK-029 - Implementar área de resumo inteligente

**Specification:** SPEC-006

**Checks obrigatórios:**

- Confirmar área de resumo no dashboard.
- Confirmar exibição quando `Insight` existe.
- Confirmar ausência controlada quando `Insight` não existe.
- Confirmar layout sem quebra com texto longo.

**Gate:** Bloqueante.

---

### TASK-030 - Configurar Spring AI/OpenAI

**Specification:** SPEC-007

**Checks obrigatórios:**

- Confirmar propriedades necessárias para Spring AI/OpenAI.
- Confirmar que credencial real não foi versionada.
- Confirmar que cliente pode ser mockado em teste.
- Confirmar que falha de configuração é tratável.
- Executar `mvn compile`.

**Gate:** Bloqueante.

---

### TASK-031 - Implementar seleção de principais entregas

**Specification:** SPEC-007

**Checks obrigatórios:**

- Confirmar seleção de subconjunto limitado de issues.
- Confirmar inclusão de título, descrição e categoria quando disponíveis.
- Confirmar comportamento com lista vazia.
- Confirmar que prompt excessivo é evitado.
- Executar teste unitário quando disponível.

**Gate:** Bloqueante.

---

### TASK-032 - Implementar prompt e InsightService

**Specification:** SPEC-007

**Checks obrigatórios:**

- Confirmar prompt com total, features, bugs, tasks e principais entregas.
- Confirmar retorno de `Insight`.
- Confirmar captura de erro da IA.
- Confirmar que serviço não calcula métricas.
- Executar teste com OpenAI mockada.

**Gate:** Bloqueante.

---

### TASK-033 - Integrar insight ao fluxo e dashboard

**Specification:** SPEC-007

**Checks obrigatórios:**

- Confirmar IA executada depois das métricas.
- Confirmar `GenerateInsightsNode` integrado ao fluxo.
- Confirmar dashboard exibe resumo quando disponível.
- Confirmar erro de IA não bloqueia cards e gráficos.
- Executar teste de fluxo com IA mockada quando disponível.

**Gate:** Bloqueante.

---

### TASK-034 - Implementar estado sem entregas

**Specification:** SPEC-008

**Checks obrigatórios:**

- Confirmar mensagem "Não existem entregas concluídas para o período informado." ou equivalente.
- Confirmar distinção entre ausência de dados e erro GitHub.
- Confirmar cards/gráficos sem quebra.
- Confirmar fluxo sem issues concluídas.

**Gate:** Bloqueante.

---

### TASK-035 - Implementar tratamento de falha GitHub

**Specification:** SPEC-008

**Checks obrigatórios:**

- Confirmar mensagem amigável para falha GitHub.
- Confirmar que token não aparece em tela nem logs.
- Confirmar que dashboard permanece acessível.
- Confirmar que cálculo não roda com dados incompletos.
- Executar teste com GitHub mockado falhando.

**Gate:** Bloqueante.

---

### TASK-036 - Implementar tratamento de falha IA

**Specification:** SPEC-008

**Checks obrigatórios:**

- Confirmar mensagem ou ausência controlada do resumo.
- Confirmar cards e gráficos visíveis após falha.
- Confirmar que exceção da IA é capturada.
- Confirmar que fluxo finaliza com `DashboardReport`.
- Executar teste com IA mockada falhando.

**Gate:** Bloqueante.

---

### TASK-037 - Implementar logging do fluxo principal

**Specification:** SPEC-008

**Checks obrigatórios:**

- Confirmar logs de início e fim da análise.
- Confirmar logs de consulta GitHub, métricas e IA.
- Confirmar log de tempo total.
- Confirmar que logs não expõem token ou segredo.
- Confirmar logs úteis em erro.

**Gate:** Bloqueante.

---

### TASK-038 - Criar testes unitários de métricas

**Specification:** SPEC-009

**Checks obrigatórios:**

- Executar testes de totalização.
- Executar testes de categoria.
- Executar teste de lista vazia.
- Executar teste de soma consistente.
- Executar `mvn test -Dtest=*Metric*` ou comando equivalente.

**Gate:** Bloqueante.

---

### TASK-039 - Criar testes de mapper e classificação

**Specification:** SPEC-009

**Checks obrigatórios:**

- Executar testes de mapper GitHub.
- Executar testes de campos opcionais ausentes.
- Executar testes de labels Feature, Bug e Task.
- Executar teste de fallback para Task.
- Executar `mvn test` ou teste específico equivalente.

**Gate:** Bloqueante.

---

### TASK-040 - Criar testes de dashboard e insight

**Specification:** SPEC-009

**Checks obrigatórios:**

- Executar teste de montagem do `DashboardReport`.
- Executar teste de resumo opcional.
- Executar teste de prompt/InsightService com IA mockada.
- Executar teste de falha da IA.
- Executar `mvn test` ou teste específico equivalente.

**Gate:** Bloqueante.

---

### TASK-041 - Criar teste de integração do fluxo principal

**Specification:** SPEC-009

**Checks obrigatórios:**

- Executar fluxo com período válido e issues simuladas.
- Executar fluxo sem dados.
- Executar fluxo com falha de IA.
- Executar fluxo com falha GitHub.
- Confirmar que GitHub e OpenAI reais não são chamados.
- Executar teste de integração específico.

**Gate:** Bloqueante.

---

## 4. Relatório Esperado por Task

Após cada task, reportar:

```text
Engineering Validation Report

Task: TASK-XXX - Nome da task
Status: PASS | FAIL | BLOCKED

Checks globais:
- [PASS|FAIL|N/A] mvn compile
- [PASS|FAIL|N/A] mvn test
- [PASS|FAIL|N/A] rastreabilidade
- [PASS|FAIL|N/A] arquitetura
- [PASS|FAIL|N/A] segurança

Checks específicos:
- [PASS|FAIL|N/A] ...

Evidências:
- ...

Próxima task liberada: Sim | Não
```

---

## 5. Política de Liberação

Uma task só libera a próxima quando:

- todos os checks obrigatórios aplicáveis estão `PASS`;
- checks não aplicáveis estão justificados;
- nenhuma falha crítica de arquitetura foi introduzida;
- nenhum segredo foi exposto;
- o código compila quando já houver base Maven;
- testes existentes passam.

