# Prompts Utilizados — DevReport

> Documentação dos principais prompts submetidos ao agente de IA (GitHub Copilot) durante a implementação do MVP.
> Cada prompt corresponde a uma task do plano de desenvolvimento.

---

## Arquitetura e Configuração (TASK-001 a TASK-006)

### TASK-001 — Criar projeto Spring Boot com Maven

**Prompt:**
```
Crie um projeto Spring Boot 3.3 usando Java 21 e Maven. O projeto deve ter:
- Classe principal com @SpringBootApplication
- pom.xml com spring-boot-starter-parent 3.3.3
- Estrutura de diretórios src/main/java e src/test/java
- Configuração para compilar com Java 21
O build deve executar sem erros com ./mvnw compile.
```

---

### TASK-002 — Configurar dependências principais

**Prompt:**
```
Adicione as seguintes dependências ao pom.xml do projeto Spring Boot:
- spring-boot-starter-web (Spring MVC + Tomcat)
- spring-boot-starter-validation (Bean Validation)
- spring-boot-starter-thymeleaf (template engine)
- spring-ai-openai-spring-boot-starter (Spring AI com OpenAI)
- langgraph4j-core (orquestração de agentes)
- openhtmltopdf-pdfbox (geração de PDF)
- spring-boot-starter-test (testes com JUnit 5 + Mockito)

Todas as dependências devem ser resolvidas corretamente. A aplicação deve iniciar sem ClassNotFoundException.
```

---

### TASK-003 — Criar estrutura de pacotes

**Prompt:**
```
Crie a seguinte estrutura de pacotes em src/main/java/com/devreport/:
- agent/        (nós do fluxo de orquestração)
- ai/           (serviços de IA e seleção de issues)
- config/       (classes de configuração Spring)
- controller/   (controllers MVC)
- domain/       (modelos de domínio, sem dependência do Spring)
- dashboard/    (montagem de relatório e exportação PDF)
- infrastructure/github/ (cliente HTTP, serviços, mappers, filtros)
- metrics/      (cálculo de métricas, classificação, gráficos)
- shared/       (utilitários compartilhados)

A estrutura deve seguir os princípios de Clean Architecture.
```

---

### TASK-004 — Criar modelos de domínio

**Prompt:**
```
Crie os seguintes modelos de domínio no pacote domain/:
- AnalysisRequest: startDate, endDate, repositories (List<String>)
- Issue: id, title, description, labels, category, closedAt, repository
- Metric: total, features, bugs, tasks
- ChartData: labels (List<String>), values (List<Integer>)
- PRMetrics: totalMerged, additions, deletions, changedFiles, averageTimeToMerge, uniqueReviewers, prSizeDistribution
- PullRequest: id, title, additions, deletions, changedFiles, mergedAt, createdAt, reviewers, repository
- RepositorySummary: repository, totalIssues, totalPRs, totalAdditions
- DashboardReport: metric, chartDataByPeriod, chartDataByCategory, insight, prMetrics, repositorySummaries, errors
- Insight: summary (String)

Todos os modelos devem ser Serializable e não depender do Spring Framework.
```

---

### TASK-005 — Criar DTOs principais

**Prompt:**
```
Crie os seguintes DTOs no pacote controller/dto/:
- AnalysisRequestDTO: startDate (LocalDate), endDate (LocalDate), repositories (List<String>)
- DashboardDTO: todos os campos para renderização no template
- IssueDTO, MetricDTO, InsightDTO

Os DTOs devem ser usados apenas para contrato controller↔view. Regras de negócio permanecem nos modelos de domínio.
```

---

### TASK-006 — Configurar propriedades do GitHub

**Prompt:**
```
Crie a classe GitHubProperties (@ConfigurationProperties) e configure application.properties com:
- github.token=${GITHUB_TOKEN}
- github.owner=IA-para-DEVs-SCTEC-T2
- github.repo=mini-projeto-modulo2-devreport
- github.project=58

O token NUNCA deve aparecer em logs. Use @ConfigurationProperties(prefix = "github").
```

---

## Entrada e Validação (TASK-007 a TASK-009)

### TASK-007 — Criar tela/formulário de período

**Prompt:**
```
Crie uma view Thymeleaf (dashboard.html) com um formulário contendo:
- Campo startDate (type="date")
- Campo endDate (type="date")
- Botão de submit "Gerar Relatório"

Use Bootstrap 5 para estilização. O formulário deve enviar POST para /.
A tela deve renderizar corretamente no navegador em http://localhost:8080.
```

---

### TASK-008 — Validar entrada de período

**Prompt:**
```
Implemente validação no AnalysisRequestDTO:
- @NotNull em startDate e endDate
- Validação customizada: endDate não pode ser anterior a startDate
- Mensagens de erro em português

No controller, use @Valid e BindingResult para tratar erros e reexibir o formulário com mensagens.
```

---

### TASK-009 — Criar controller do dashboard

**Prompt:**
```
Crie o DashboardController (@Controller) com:
- GET / → renderiza dashboard.html com formulário vazio
- POST / → recebe AnalysisRequestDTO, valida, e inicia análise

O controller deve ser fino — sem regras de negócio. Injetar o DevReportAgent para orquestrar o fluxo.
```

---

## Integração GitHub Issues (TASK-010 a TASK-014)

### TASK-010 — Implementar cliente HTTP do GitHub

**Prompt:**
```
Crie o GitHubClient no pacote infrastructure/github/ usando RestClient do Spring Boot 3.
Deve:
- Configurar header Authorization: Bearer {token} em todas as requisições
- Base URL: https://api.github.com
- Método genérico para GET com query parameters
- Token NUNCA deve aparecer em logs (use @Slf4j com cuidado)
```

---

### TASK-011 — Implementar consulta de issues concluídas

**Prompt:**
```
Implemente GitHubIssueService com método para buscar issues fechadas da API:
GET /repos/{owner}/{repo}/issues?state=closed&assignee={assignee}

- Issues devem estar fechadas (state=closed)
- Filtrar por assignee configurável
- Capturar erros HTTP e retornar lista vazia em caso de falha
- Suportar paginação se necessário
```

---

### TASK-012 — Implementar mapper de GitHub para domínio

**Prompt:**
```
Crie GitHubIssueMapper para converter JSON da API GitHub → modelo Issue do domínio:
- id, title, body → description
- labels[] → extrair nomes para classificação posterior
- closed_at → closedAt
- repository → adicionar campo owner/repo de origem

Campos opcionais ausentes não devem quebrar a conversão.
```

---

### TASK-013 — Implementar filtro por período

**Prompt:**
```
Crie IssueFilter com método para filtrar issues por período:
- Manter apenas issues com closedAt entre startDate e endDate (inclusive)
- Issues sem closedAt devem ser removidas
- Usar Stream API para filtrar de forma declarativa
```

---

### TASK-014 — Tratar erros básicos da integração GitHub

**Prompt:**
```
Implemente tratamento de erros no GitHubIssueService:
- HTTP 401/403 → mensagem amigável "Token GitHub inválido ou expirado"
- HTTP 404 → mensagem "Repositório não encontrado"
- Timeout / Connection Refused → mensagem "GitHub indisponível no momento"

NUNCA expor o token ou stack trace completo nas mensagens de erro.
```

---

## Métricas de Issues (TASK-015 a TASK-018)

### TASK-015 — Implementar classificador de issues

**Prompt:**
```
Crie IssueClassifier no pacote metrics/:
- Se labels contém "feature" → Category.FEATURE
- Se labels contém "bug" → Category.BUG
- Se labels contém "task" → Category.TASK
- Fallback: qualquer label desconhecida ou ausente → Category.TASK

A classificação deve ser case-insensitive.
```

---

### TASK-016 — Implementar cálculo de métricas consolidadas

**Prompt:**
```
Crie MetricsService no pacote metrics/:
- Calcular Metric(total, features, bugs, tasks) a partir da lista classificada
- total = issues.size()
- features = count(category == FEATURE)
- bugs = count(category == BUG)
- tasks = count(category == TASK)
- features + bugs + tasks deve sempre igualar total
- Lista vazia → todos os campos = 0
```

---

### TASK-017 — Implementar dados de gráfico por período

**Prompt:**
```
Crie ChartDataService com método para agrupar issues por dia/semana:
- Retornar ChartData com labels (datas formatadas "dd/MM") e values (contagem)
- Issues agrupadas por closedAt
- Ordenar cronologicamente
- Lista vazia → labels e values vazios
```

---

### TASK-018 — Implementar dados de gráfico por categoria

**Prompt:**
```
Crie método no ChartDataService para distribuição por categoria:
- ChartData com labels = ["Features", "Bugs", "Tasks"] e values = [counts]
- Valores devem bater exatamente com Metric calculado
- Lista vazia → values = [0, 0, 0]
```

---

## Orquestração com LangGraph4j (TASK-019 a TASK-022)

### TASK-019 — Criar estado AnalysisState

**Prompt:**
```
Crie a classe AnalysisState no pacote agent/ como um Record ou classe com builder:
Campos:
- startDate, endDate (LocalDate)
- repositories (List<String>)
- issues (List<Issue>)
- pullRequests (List<PullRequest>)
- metric (Metric)
- prMetrics (PRMetrics)
- chartDataByPeriod, chartDataByCategory (ChartData)
- repositorySummaries (List<RepositorySummary>)
- insight (Insight)
- dashboardReport (DashboardReport)
- errors (List<String>)
- completed (boolean)

Deve implementar Serializable. Use reducers adequados para listas (append) vs valores únicos (overwrite).
```

---

### TASK-020 — Implementar nodes do LangGraph4j

**Prompt:**
```
Implemente 6 nodes como @Component Spring implementando NodeAction<AnalysisState>:

1. StartNode — inicializa o estado com request (startDate, endDate, repositories)
2. ValidateRequestNode — valida datas, define errors se inválido
3. FetchGitHubDataNode — consulta GitHub para issues e PRs, preenche as listas
4. CalculateMetricsNode — calcula Metric, PRMetrics, ChartData, RepositorySummary
5. GenerateInsightsNode — chama InsightService para gerar resumo IA
6. BuildDashboardNode — consolida DashboardReport final

Cada node deve ter responsabilidade única. Nodes usam os serviços injetados.
Erro no GenerateInsightsNode NÃO deve bloquear o fluxo.
```

---

### TASK-021 — Montar fluxo principal do agente

**Prompt:**
```
Crie DevReportAgent (@Service) que monta o StateGraph do LangGraph4j:

Fluxo:
START → start → validate → (erro?) → buildDashboard → END
                          → (ok) → fetchGitHubData → calculateMetrics
                                   → (erro?) → buildDashboard → END
                                   → (ok) → generateInsights → buildDashboard → END

Use addConditionalEdges com errorRouter nos pontos de validação e cálculo.
O grafo deve ser compilado UMA vez no construtor (@PostConstruct).
Método analyze(request) injeta request no StartNode e dispara compiledGraph.invoke().
```

---

### TASK-022 — Integrar controller com agente

**Prompt:**
```
Integre o DashboardController com DevReportAgent:
- POST / recebe AnalysisRequestDTO
- Constrói AnalysisRequest
- Chama agent.analyze(request)
- Recebe DashboardReport do estado
- Mapeia para DashboardDTO e envia para o template

Em caso de erro, adicionar mensagem amigável ao modelo.
```

---

## Dashboard Web (TASK-023 a TASK-029)

### TASK-023 — Criar serviço de montagem do DashboardReport

**Prompt:**
```
Crie DashboardService no pacote dashboard/:
- Consolidar Metric, ChartData, PRMetrics, RepositorySummary, Insight em DashboardReport
- Se não houver issues, definir mensagem "Não existem entregas concluídas para o período informado."
- O report deve ser serializável e pronto para consumo pela view
```

---

### TASK-024 — Criar layout base Thymeleaf

**Prompt:**
```
Crie o template dashboard.html usando Thymeleaf + Bootstrap 5:
- Navbar com título "DevReport"
- Formulário de período com date inputs
- Área de resultados com cards, gráficos e resumo
- Layout responsivo (grid Bootstrap)
- CSS customizado para cards de métricas com cores: azul (total), verde (features), vermelho (bugs), amarelo (tasks)
```

---

### TASK-025 — Implementar cards de métricas

**Prompt:**
```
Adicione cards de métricas no dashboard.html:
- Card "Total de Entregas" com valor de metric.total
- Card "Features" com valor de metric.features (verde)
- Card "Bugs" com valor de metric.bugs (vermelho)
- Card "Tasks" com valor de metric.tasks (amarelo)

Use th:text para injetar valores. Valores zerados devem aparecer como "0".
```

---

### TASK-026 — Integrar Chart.js

**Prompt:**
```
Adicione Chart.js ao dashboard.html:
- CDN: <script src="https://cdn.jsdelivr.net/npm/chart.js">
- Preparar <canvas> elements para gráficos de período e categoria
- Injetar dados via Thymeleaf com th:inline="javascript"
- Inicializar gráficos no evento DOMContentLoaded
```

---

### TASK-027 — Renderizar gráfico de entregas por período

**Prompt:**
```
Implemente gráfico de barras (Chart.js) para entregas por período:
- Labels vindos de chartDataByPeriod.labels (datas)
- Valores de chartDataByPeriod.values
- Cor azul (#0d6efd)
- Eixo Y começa do zero
- Gráfico responsivo
```

---

### TASK-028 — Renderizar gráfico de distribuição por categoria

**Prompt:**
```
Implemente gráfico de pizza/doughnut (Chart.js) para distribuição por categoria:
- Labels: ["Features", "Bugs", "Tasks"]
- Valores de chartDataByCategory.values
- Cores: verde (#198754), vermelho (#dc3545), amarelo (#ffc107)
- Mostrar legenda e tooltips
```

---

### TASK-029 — Implementar área de resumo inteligente

**Prompt:**
```
Adicione seção de resumo inteligente no dashboard.html:
- Card com título "Resumo Inteligente (IA)"
- Se insight.summary não for nulo, renderizar com th:utext (permite HTML)
- Se insight for nulo, exibir mensagem "O resumo inteligente não pôde ser gerado, mas as métricas estão disponíveis."
- Estilizar com borda azul clara e ícone de estrela
```

---

## Inteligência Artificial (TASK-030 a TASK-033)

### TASK-030 — Configurar Spring AI/OpenAI

**Prompt:**
```
Configure Spring AI no projeto:
- Adicionar spring-ai-openai-spring-boot-starter ao pom.xml
- Criar OpenAiConfig (@Configuration) com ChatClient bean
- Propriedades em application.properties:
  spring.ai.openai.api-key=${AI_API_KEY}
  spring.ai.openai.base-url=${AI_BASE_URL:https://api.deepseek.com}
  spring.ai.openai.chat.options.model=${AI_MODEL:deepseek-chat}
  spring.ai.openai.chat.options.temperature=${AI_TEMPERATURE:0.7}
- API key NUNCA deve aparecer em logs
```

---

### TASK-031 — Implementar seleção de principais entregas

**Prompt:**
```
Crie IssueSelector no pacote ai/:
- Selecionar até 5 issues mais relevantes (priorizar features > bugs > tasks)
- Para cada issue selecionada, extrair: título, descrição (primeiros 200 chars) e categoria
- Retornar lista formatada para inclusão no prompt da IA
- Lista vazia → retornar string "Nenhuma entrega no período."
```

---

### TASK-032 — Implementar prompt e InsightService

**Prompt:**
```
Crie InsightService (@Service) no pacote ai/:

Prompt template:
"Você é um analista de produtividade. Analise as seguintes métricas:
- Período: {startDate} a {endDate}
- Total de entregas: {total} (Features: {features}, Bugs: {bugs}, Tasks: {tasks})
- PRs merged: {prTotal}
- Repositórios analisados: {repoCount}
- Principais entregas: {selectedIssues}

Gere um resumo executivo em português com 3 seções em HTML:
1. Destaques do período
2. Métricas principais
3. Valor entregue

Formato: <div class='highlight'>...</div><div class='metrics'>...</div><div class='value'>...</div>"

Se MOCK_MODE=true, gere resumo fallback com String.format() usando os dados reais.
Se a API falhar, retorne null (GenerateInsightsNode trata).
```

---

### TASK-033 — Integrar insight ao fluxo e dashboard

**Prompt:**
```
Conecte InsightService ao GenerateInsightsNode:
- Após CalculateMetricsNode, o fluxo vai para GenerateInsightsNode
- Chama insightService.generateInsight(state)
- Se retornar Insight, adiciona ao estado
- Se lançar exceção, loga o erro mas NÃO bloqueia o fluxo
- BuildDashboardNode inclui o insight (ou null) no DashboardReport
```

---

## Tratamento de Erros e Logging (TASK-034 a TASK-037)

### TASK-034 — Implementar estado sem entregas

**Prompt:**
```
Implemente estado vazio no BuildDashboardNode:
- Se issues.isEmpty(), definir mensagem "Não existem entregas concluídas para o período informado."
- Cards devem mostrar valores zerados
- Gráficos devem ser escondidos ou mostrar "Sem dados"
- O estado vazio deve ser distinto de erro GitHub (mensagem diferente)
```

---

### TASK-035 — Implementar tratamento de falha GitHub

**Prompt:**
```
Implemente tratamento de erro GitHub no FetchGitHubDataNode:
- Capturar exceções do GitHubIssueService
- Adicionar mensagem amigável ao state.errors
- NÃO adicionar o token ou stack trace
- O fluxo deve ir direto para BuildDashboardNode (via errorRouter)
- Dashboard deve exibir mensagem "Não foi possível consultar os dados do GitHub no momento."
```

---

### TASK-036 — Implementar tratamento de falha IA

**Prompt:**
```
Torne o InsightService resiliente:
- Capturar todas as exceções (rede, timeout, API key inválida, rate limit)
- Logar o erro com nível WARN
- Retornar null
- GenerateInsightsNode não adiciona erro ao state.errors quando insight é null
- Dashboard renderiza cards e gráficos normalmente, apenas sem resumo IA
```

---

### TASK-037 — Implementar logging do fluxo principal

**Prompt:**
```
Adicione logging (@Slf4j) em todos os nodes e serviços principais:
- Início/fim de cada node com tempo de execução
- Quantidade de issues e PRs encontrados
- Métricas calculadas
- Erros com mensagens amigáveis

Use log levels:
- INFO: início/fim de node, contagens
- WARN: falhas não-bloqueantes (IA)
- ERROR: falhas críticas (GitHub)

NUNCA logar tokens, API keys ou dados sensíveis.
```

---

## Testes do MVP (TASK-038 a TASK-041)

### TASK-038 — Criar testes unitários de métricas

**Prompt:**
```
Crie MetricsServiceTest com JUnit 5 + Mockito:
- Testar cálculo com 5 issues (2 features, 2 bugs, 1 task)
- Testar lista vazia retorna zeros
- Testar todas as issues como features
- Testar fallback para Task (label desconhecida)
- Testar que total = features + bugs + tasks
- Testar com 50 issues (performance)

Use @ExtendWith(MockitoExtension.class) e @InjectMocks.
```

---

### TASK-039 — Criar testes de mapper e classificação

**Prompt:**
```
Crie GitHubIssueMapperTest e IssueClassifierTest:

GitHubIssueMapperTest:
- Mapear issue completa com todos os campos
- Mapear issue com campos opcionais nulos (não quebrar)
- Verificar que labels são extraídas como lista de strings
- Verificar que repository é preenchido corretamente

IssueClassifierTest:
- Classificar label "feature" → FEATURE
- Classificar label "bug" → BUG
- Classificar label "task" → TASK
- Classificar label "enhancement" → TASK (fallback)
- Classificar sem labels → TASK (fallback)
- Case-insensitive: "Bug", "BUG", "bug" → BUG
```

---

### TASK-040 — Criar testes de dashboard e insight

**Prompt:**
```
Crie DashboardServiceTest e InsightServiceTest:

DashboardServiceTest:
- Testar montagem com métricas populadas
- Testar montagem sem insight (insight = null)
- Testar estado vazio (sem issues) gera mensagem apropriada
- Testar com PR metrics presentes

InsightServiceTest:
- Testar modo mock gera resumo com dados reais (verificar String.format)
- Testar modo mock com lista vazia
- Testar que falha de API retorna null
- Testar que prompt contém dados de métricas
```

---

### TASK-041 — Criar teste de integração do fluxo principal

**Prompt:**
```
Crie FullFlowIntegrationTest (@SpringBootTest):
- Simular GitHub com mock (Mockito)
- Simular IA com mock (Mockito)
- Testar fluxo completo: request válido → dashboard com métricas
- Testar período sem dados → estado vazio
- Testar falha GitHub → mensagem de erro amigável
- Testar falha IA → dashboard com métricas mas sem resumo
- Usar @AutoConfigureMockMvc para testar controller
```

---

## Multi-Repo Support (TASK-042 a TASK-046)

### TASK-042 — Implementar endpoint de listagem de repositórios

**Prompt:**
```
Adicione método no GitHubClient para listar repositórios do owner:
GET /orgs/{owner}/repos?sort=updated&per_page=30

Crie endpoint GET /api/repositories no DashboardController que retorna a lista.
Formato: [{ "fullName": "owner/repo", "name": "repo" }]
A resposta deve ser JSON serializável.
```

---

### TASK-043 — Implementar seleção multi-repo no formulário

**Prompt:**
```
Adicione campo multi-select no formulário dashboard.html:
- Populado via JavaScript fetch('/api/repositories')
- Usar selectpicker ou checkboxes do Bootstrap
- Campo repositories[] enviado como List<String> no POST
- Se nenhum repositório selecionado, usar o padrão do application.properties
- Campo deve ser opcional
```

---

### TASK-044 — Adaptar GitHubClient para multi-repo

**Prompt:**
```
Modifique GitHubIssueService para aceitar lista de repositórios:
- Iterar sobre repositories (se vazio, usar [github.repo])
- Para cada repo, buscar issues e PRs
- Adicionar campo "repository" em cada item
- Falha em um repositório NÃO deve interromper os demais
- Logar warning "Falha ao consultar repositório {repo}: {mensagem}"
```

---

### TASK-045 — Implementar agregação e identificação de origem

**Prompt:**
```
Garantir rastreabilidade de origem em todas as entidades:
- Issue.repository = "owner/repo"
- PullRequest.repository = "owner/repo"
- RepositorySummary.repository = "owner/repo"

Após coleta de todos os repositórios, agregar as listas:
- List<Issue> todas = Stream.of(repo1, repo2...).flatMap(List::stream).toList()
- Manter ordem de coleta
- Possibilitar filtro por repositório após agregação
```

---

### TASK-046 — Implementar cálculo de RepositorySummary

**Prompt:**
```
Crie método no MetricsService para calcular RepositorySummary:
- Para cada repositório distinto, calcular:
  - totalIssues: count de issues com aquele repository
  - totalPRs: count de PRs com aquele repository
  - totalAdditions: soma de additions de PRs daquele repositório
- Retornar List<RepositorySummary> ordenada por totalIssues decrescente
- Lista vazia → retornar lista vazia
```

---

## PR Analytics (TASK-047 a TASK-052)

### TASK-047 — Implementar consulta de PRs merged

**Prompt:**
```
Implemente GitHubPRService com consulta à API:
GET /repos/{owner}/{repo}/pulls?state=closed&sort=updated&direction=desc&per_page=100

Para cada PR retornado, verificar se merged_at existe e está dentro do período.
Filtrar apenas merged PRs (merged_at != null).
Coletar dados detalhados: additions, deletions, changed_files, requested_reviewers.
```

---

### TASK-048 — Implementar mapper de PRs

**Prompt:**
```
Crie GitHubPRMapper para converter JSON → PullRequest:
- title, number → id
- additions, deletions, changed_files → changedFiles
- merged_at → mergedAt, created_at → createdAt
- requested_reviewers[] → extrair logins para List<String> reviewers
- repository → adicionado pelo serviço

Campos opcionais nulos não devem quebrar a conversão.
```

---

### TASK-049 — Implementar PRMetricsService

**Prompt:**
```
Crie PRMetricsService no pacote metrics/:
- totalMerged = prs.size()
- additions = sum(pr.additions)
- deletions = sum(pr.deletions)
- changedFiles = sum(pr.changedFiles)
- averageTimeToMerge = média de (mergedAt - createdAt) em horas
- uniqueReviewers = count distintos de todos os reviewers
- prSizeDistribution:
  - small: pr.additions < 100
  - medium: 100 <= pr.additions < 500
  - large: pr.additions >= 500

Lista vazia → todos os campos = 0
```

---

### TASK-050 — Implementar seção PR Analytics no dashboard

**Prompt:**
```
Adicione seção "PR Analytics" no dashboard.html:
Cards:
- PRs Merged (totalMerged)
- Linhas Alteradas (additions + deletions)
- Tempo Médio até Merge (formatar "Xh Ym")
- Revisores Distintos (uniqueReviewers)
- Cobertura de Review (PRs com reviewer / total PRs) %

Seção visível apenas quando prMetrics.totalMerged > 0.
Usar ícones do Bootstrap para cada card.
```

---

### TASK-051 — Implementar gráfico de distribuição de PRs

**Prompt:**
```
Adicione gráfico de barras Chart.js para distribuição de tamanho de PR:
- Labels: ["Pequeno (<100)", "Médio (100-499)", "Grande (500+)"]
- Valores de prSizeDistribution
- Cores: verde, amarelo, vermelho
- Gráfico visível apenas quando prMetrics.totalMerged > 0
```

---

### TASK-052 — Atualizar InsightService com contexto multi-repo e PR

**Prompt:**
```
Atualize o prompt do InsightService para incluir:
- Métricas de PR: "PRs merged: {count}, Total de linhas alteradas: {lines}"
- Contexto multi-repo: "Repositórios analisados: {count}"
- Destaque do repo com mais contribuições: "Destaque: {repo} com {count} entregas"

O resumo gerado (mock ou IA) deve:
- Mencionar a quantidade de repositórios
- Destacar o repositório com maior contribuição
- Incluir dados de PR quando disponíveis
```

---

## Exportação PDF (TASK-053 a TASK-055)

### TASK-053 — Implementar endpoint de exportação PDF

**Prompt:**
```
Crie endpoint GET /dashboard/export no DashboardController:
- Aceitar startDate, endDate, repositories (mesmos params do POST)
- Reexecutar o fluxo de análise via agent.analyze()
- Chamar pdfService.generatePdf(report)
- Response:
  - Content-Type: application/pdf
  - Content-Disposition: attachment; filename="devreport-{startDate}-{endDate}.pdf"
- Disparar download no navegador
```

---

### TASK-054 — Criar template Thymeleaf otimizado para PDF

**Prompt:**
```
Crie pdf-report.html no templates/:
- CSS @media print com @page para A4 (210mm x 297mm)
- Margens: 15mm
- Cabeçalho com "DevReport" e período
- Rodapé com número de página e data de geração
- Cards KPI inline-block (22% width cada)
- Tabela de legenda das métricas
- Seção PR Analytics
- Gráficos substituídos por tabelas (SVG não funciona bem em PDF)
- Resumo IA com classes .highlight, .metrics, .value
- Elementos interativos (botões, formulários) ocultados
- Fonte: Arial/Helvetica, 10-12pt
```

---

### TASK-055 — Implementar conversão HTML para PDF

**Prompt:**
```
Crie PdfService (@Service) no pacote dashboard/:
- Renderizar pdf-report.html com TemplateEngine do Thymeleaf
- Sanitizar conteúdo: remover caracteres de controle ([\x00-\x08\x0B\x0C\x0E-\x1F])
- Substituir &nbsp; por &#160; e &mdash; por —
- Converter HTML → PDF usando PdfRendererBuilder (openhtmltopdf)
- Retornar byte[] do PDF gerado

Tratar erros de conversão e retornar PDF com mensagem de erro se necessário.
```

---

## Testes das Novas Features (TASK-056 a TASK-058)

### TASK-056 — Implementar teste de falha em um repositório

**Prompt:**
```
Crie MultiRepoResilienceTest:
- Simular 3 repositórios: repo-A (sucesso), repo-B (falha 404), repo-C (sucesso)
- Verificar que issues e PRs de repo-A e repo-C são coletados
- Verificar que repo-B é logado como warning
- Dashboard final tem dados agregados de repo-A + repo-C
- Mensagem parcial indica falha em repo-B
```

---

### TASK-057 — Criar testes para PRMetricsService

**Prompt:**
```
Crie PRMetricsServiceTest:
- Testar cálculo com 3 PRs de tamanhos diferentes (small, medium, large)
- Testar averageTimeToMerge com PRs simulados
- Testar uniqueReviewers sem duplicatas
- Testar PRs sem revisores
- Testar lista vazia → todas métricas zeradas
- Testar distribuição de tamanho com PRs no limite (exactly 100, exactly 500)
```

---

### TASK-058 — Criar teste de integração do fluxo completo

**Prompt:**
```
Crie teste de integração FullFlowIntegrationTest completo:
- Simular 2 repositórios com issues e PRs mockados
- Validar fluxo completo: request → agent → dashboard → PDF
- Verificar RepositorySummary para cada repositório
- Verificar PRMetrics consistente
- Verificar PDF gerado sem erro
- Simular falha em um repositório e verificar resiliência
- Usar @SpringBootTest com @TestConfiguration para mocks
```

---

## Correções Finais e Polimento (TASK-059)

### TASK-059 — Correções finais de encoding, modo mock IA e polimento do MVP

**Prompt:**
```
Faça as correções finais do MVP:

1. Remover BOM de todos os arquivos Java:
   - Verificar com file -bi *.java | grep "BOM"
   - Salvar como UTF-8 sem BOM

2. Implementar modo mock no InsightService:
   - @Value("${devreport.mock-mode:true}") boolean mockMode
   - Se mockMode=true, chamar buildFallbackSummary() diretamente
   - Método gera HTML com String.format() usando métricas reais
   - Mesmo formato de 3 seções: highlight, metrics, value

3. Consolidar dependências no pom.xml:
   - Remover duplicatas
   - Verificar versões compatíveis
   - Garantir que spring-ai-openai está no repositório Spring Milestones

4. Refinar templates:
   - dashboard.html: ajustar cards Quick Win (throughput, velocidade, densidade)
   - pdf-report.html: adicionar legenda de métricas e seção PR

5. Ajustar controller e DTOs:
   - DashboardController: ContentDisposition.attachment() para PDF
   - AnalysisRequestDTO: validação com mensagens em português

Verificação final:
- mvnw.cmd compile → OK
- mvnw.cmd test → 62 testes passando
- MOCK_MODE=true → aplicação funcional sem tokens
```

---

## Documentação Final (TASK-060 a TASK-061)

### TASK-060 — Implementar README com explicação

**Prompt:**
```
Crie um README.md profissional e completo para o projeto DevReport com as seguintes seções:

1. Badges: Java 21, Spring Boot 3.3, licença MIT
2. "Sobre": explicar que o DevReport transforma dados do GitHub em dashboard de produtividade
3. "Arquitetura": diagrama ASCII da Clean Architecture (Presentation → Application → Domain → Infrastructure)
4. "Estrutura de Pacotes": árvore de diretórios de src/main/java/com/devreport/
5. "Stack Tecnológica": tabela com Java 21, Spring Boot 3.3, Thymeleaf, Chart.js, Spring AI, LangGraph4j, openhtmltopdf, Maven, JUnit 5
6. "Fluxo do Agente (LangGraph4j)": diagrama do StateGraph com 6 nodes, explicação de cada node, roteamento condicional, schema do AnalysisState
7. "Como Executar": pré-requisitos (Java 21, Maven, token GitHub), tabela de variáveis de ambiente, comandos Maven (compile, test, run)
8. "O que o Dashboard Exibe": seções de Issues (cards, quick wins, gráficos), Resumo IA, PR Analytics, Exportação PDF
9. "Testes": tabela com categorias de teste e contagens (62 testes)

O README deve servir como porta de entrada para novos desenvolvedores e stakeholders.
```

---

### TASK-061 — Documentar prompts utilizados no desenvolvimento

**Prompt:**
```
Crie o arquivo docs/prompts.md documentando todos os prompts submetidos ao agente de IA durante o desenvolvimento do MVP.

Requisitos:
- Um prompt por task, de TASK-001 a TASK-059
- Cada prompt deve conter o código/instrução exata submetida (formato realista, como se tivesse sido enviado ao Copilot)
- Organizar por seções seguindo os épicos do projeto:
  - Arquitetura e Configuração (TASK-001 a 006)
  - Entrada e Validação (TASK-007 a 009)
  - Integração GitHub Issues (TASK-010 a 014)
  - Métricas de Issues (TASK-015 a 018)
  - Orquestração com LangGraph4j (TASK-019 a 022)
  - Dashboard Web (TASK-023 a 029)
  - Inteligência Artificial (TASK-030 a 033)
  - Tratamento de Erros e Logging (TASK-034 a 037)
  - Testes do MVP (TASK-038 a 041)
  - Multi-Repo Support (TASK-042 a 046)
  - PR Analytics (TASK-047 a 052)
  - Exportação PDF (TASK-053 a 055)
  - Testes das Novas Features (TASK-056 a 058)
  - Correções Finais e Polimento (TASK-059)
- Incluir índice rápido com tabela Task × Área × Prompt
- Usar blocos de código (```) para os prompts
- Cabeçalho com título e descrição do propósito do arquivo
- Total de prompts documentados no rodapé
```

---

## Índice Rápido

| Task | Área | Prompt |
|------|------|--------|
| TASK-001 | Config | Projeto Spring Boot |
| TASK-002 | Config | Dependências |
| TASK-003 | Config | Pacotes |
| TASK-004 | Config | Domínio |
| TASK-005 | Config | DTOs |
| TASK-006 | Config | GitHub Properties |
| TASK-007 | UI | Formulário |
| TASK-008 | UI | Validação |
| TASK-009 | UI | Controller |
| TASK-010 | GitHub | HTTP Client |
| TASK-011 | GitHub | Issues Query |
| TASK-012 | GitHub | Mapper |
| TASK-013 | GitHub | Filtro Período |
| TASK-014 | GitHub | Error Handling |
| TASK-015 | Métricas | Classificador |
| TASK-016 | Métricas | Cálculo |
| TASK-017 | Métricas | Gráfico Período |
| TASK-018 | Métricas | Gráfico Categoria |
| TASK-019 | Agent | AnalysisState |
| TASK-020 | Agent | Nodes |
| TASK-021 | Agent | Fluxo |
| TASK-022 | Agent | Integração |
| TASK-023 | Dashboard | Report Service |
| TASK-024 | Dashboard | Layout |
| TASK-025 | Dashboard | Cards |
| TASK-026 | Dashboard | Chart.js |
| TASK-027 | Dashboard | Gráfico Período |
| TASK-028 | Dashboard | Gráfico Categoria |
| TASK-029 | Dashboard | Resumo IA |
| TASK-030 | IA | Spring AI Config |
| TASK-031 | IA | Issue Selector |
| TASK-032 | IA | Insight Service |
| TASK-033 | IA | Integração |
| TASK-034 | Erros | Estado Vazio |
| TASK-035 | Erros | Falha GitHub |
| TASK-036 | Erros | Falha IA |
| TASK-037 | Erros | Logging |
| TASK-038 | Testes | Métricas |
| TASK-039 | Testes | Mapper |
| TASK-040 | Testes | Dashboard/IA |
| TASK-041 | Testes | Integração |
| TASK-042 | Multi-Repo | Listar |
| TASK-043 | Multi-Repo | Seleção UI |
| TASK-044 | Multi-Repo | Adaptar Client |
| TASK-045 | Multi-Repo | Agregação |
| TASK-046 | Multi-Repo | Summary |
| TASK-047 | PR | Consulta |
| TASK-048 | PR | Mapper |
| TASK-049 | PR | Métricas |
| TASK-050 | PR | Dashboard |
| TASK-051 | PR | Gráfico |
| TASK-052 | PR | IA + PR |
| TASK-053 | PDF | Endpoint |
| TASK-054 | PDF | Template |
| TASK-055 | PDF | Conversão |
| TASK-056 | Testes | Multi-Repo Resiliência |
| TASK-057 | Testes | PR Metrics |
| TASK-058 | Testes | Integração Completa |
| TASK-059 | Polish | Correções Finais |
| TASK-060 | Docs | README |
| TASK-061 | Docs | Prompts |

---
> **Total: 61 prompts documentados para 61 tasks.**
