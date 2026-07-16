# Technical Design Document (TDD)

# DevReport

Versão: 1.0

Data: Julho/2026

Autor: Thiago Carlos Andrade

---

# 1. Objetivo

Este documento descreve a arquitetura técnica do DevReport.

Seu objetivo é servir como referência para implementação, manutenção e evolução do sistema.

---

# 2. Arquitetura

O sistema seguirá uma arquitetura em camadas baseada nos princípios da Clean Architecture.

```
Presentation
        │
Application
        │
Domain
        │
Infrastructure
```

Cada camada possuirá responsabilidades bem definidas.

---

# 3. Stack Tecnológica

## Backend

- Java 21
- Spring Boot 3.x

## Inteligência Artificial

- Spring AI
- OpenAI GPT

## Orquestração

- LangGraph4j

## Frontend

- Thymeleaf
- Bootstrap 5
- Chart.js

## Integrações

- GitHub REST API

## Build

- Maven

---

# 4. Estrutura do Projeto

```
src/main/java

br.com.devreport

config

controller

application

domain

infrastructure

github

metrics

dashboard

ai

agent

shared
```

---

# 5. Camadas

## Presentation

Responsável pela interface.

Exemplos

- Controllers
- Views Thymeleaf

Não contém regra de negócio.

---

## Application

Responsável pelos casos de uso.

Exemplos

- GenerateDashboardUseCase
- AnalyzeRepositoryUseCase

Coordena os serviços.

---

## Domain

Contém apenas regras de negócio.

Exemplos

Issue

Metric

DashboardReport

Insight

Project

Sem dependência do Spring.

---

## Infrastructure

Implementa integrações externas.

Exemplos

GitHub API

Spring AI

OpenAI

---

# 6. Modelo de Domínio

## AnalysisRequest

Representa uma solicitação de análise.

Campos

- startDate
- endDate
- repositories (List<String>)

---

## Issue

Representa uma Issue recuperada do GitHub.

Campos

- id
- title
- description
- labels
- closedAt
- author
- repository (String - identificador do repositório de origem)

---

## PullRequest

Representa um Pull Request merged recuperado do GitHub.

Campos

- id
- number
- title
- createdAt
- mergedAt
- additions
- deletions
- changedFiles
- reviewers (List<String>)
- reviewComments
- labels
- repository (String)

---

## Metric

Representa uma métrica calculada.

Campos

- total
- features
- bugs
- tasks

---

## PRMetrics

Representa métricas consolidadas de Pull Requests.

Campos

- totalMerged
- totalAdditions
- totalDeletions
- totalChangedFiles
- averageTimeToMerge (horas)
- uniqueReviewers (quantidade de revisores distintos)
- prSizeDistribution (pequeno: <100, médio: 100-500, grande: >500 linhas)

---

## RepositorySummary

Representa a contribuição de um repositório individual.

Campos

- name (owner/repo)
- totalIssues
- totalPRs
- totalAdditions

---

## DashboardReport

Representa o resultado final.

Campos

- metrics
- prMetrics
- repositorySummaries (List<RepositorySummary>)
- charts
- summary
- repositoriesCount

---

## Insight

Representa o texto gerado pela IA.

---

# 7. Fluxo Principal

```
Usuário

↓

Controller

↓

LangGraph Agent

↓

GitHub API

↓

Metrics Engine

↓

Spring AI

↓

Dashboard
```

---

# 8. Fluxo do Agente

O LangGraph4j será responsável por coordenar toda a execução.

Nodes

## StartNode

Recebe a solicitação.

↓

## ValidateRequestNode

Valida período informado.

↓

## FetchGitHubDataNode

Consulta GitHub.

↓

## CalculateMetricsNode

Calcula indicadores.

↓

## GenerateInsightsNode

Executa IA.

↓

## BuildDashboardNode

Monta Dashboard.

↓

End

---

# 9. Agent State

O estado compartilhado entre os Nodes conterá:

```
AnalysisState

startDate

endDate

issues

metrics

summary

dashboard
```

Cada Node poderá ler e atualizar esse estado.

---

# 10. GitHub Integration

Responsabilidades

- consultar Issues
- consultar Pull Requests merged
- consultar Projects
- consultar Labels
- listar repositórios disponíveis do usuário/org

Nunca deverá conter regra de negócio.

**Multi-Repo:** o cliente deve aceitar uma lista de repositórios e iterar sobre cada um, retornando dados agregados.

---

# 11. Metrics Engine

Responsável por calcular indicadores.

Entradas

- List<Issue>
- List<PullRequest>

Saídas

- Metric
- PRMetrics
- List<RepositorySummary>

Indicadores de Issue

- total
- features
- bugs
- tasks
- entregas por período

Indicadores de PR

- total merged
- linhas alteradas (additions + deletions)
- tempo médio até merge
- total de revisores distintos
- distribuição por tamanho (pequeno/médio/grande)

Indicadores por Repositório

- issues por repo
- PRs por repo
- linhas alteradas por repo

---

# 12. Spring AI

Responsável por produzir análises.

Entrada

Metric

Issue

Saída

Insight

Prompt

"O usuário realizou X entregas.
Crie um resumo executivo."

---

# 13. Dashboard

Responsável por apresentar os resultados.

**Seção Issues**

Cards

- Total

- Features

- Bugs

- Tasks

Gráficos

- Entregas por período

- Distribuição por categoria

**Seção PR Analytics**

Cards

- PRs merged

- Linhas alteradas

- Tempo médio até merge

- Revisores distintos

Gráfico

- Distribuição por tamanho de PR

**Indicador de repositórios**

- Quantidade e lista de repositórios analisados

**Resumo**

- IA com contexto de issues, PRs e múltiplos repositórios

**Ações**

- Botão "Baixar Relatório PDF"

---

# 14. Export PDF

Responsável por gerar um arquivo PDF formatado do relatório.

**Endpoint:** `GET /dashboard/export?start=...&end=...&repositories=...`

**Fluxo:**

1. Receber os mesmos parâmetros da análise.
2. Reutilizar os dados já computados do `DashboardReport`.
3. Renderizar template HTML otimizado para impressão (CSS `@media print`, dimensões A4).
4. Converter HTML para PDF utilizando Flying Saucer ou Thymeleaf + biblioteca de renderização.
5. Retornar PDF como download.

**Conteúdo do PDF:**

- Cabeçalho: nome do desenvolvedor + período + repositórios analisados
- Cards KPI (issues e PRs)
- Gráficos (renderizados como SVG embutido)
- Tabela de repositórios
- Resumo da IA

**Observação:** Se o relatório ainda não foi carregado no dashboard, o endpoint deve computar os dados primeiro antes de gerar o PDF.

---

# 15. Controllers

## DashboardController

Responsabilidades

- receber requisição

- iniciar análise

- retornar View

---

# 16. Services

GitHubService (issues + PRs + listagem de repositórios)

↓

MetricsService (issues + PRs + por repositório)

↓

InsightService (com contexto multi-repo e PR)

↓

DashboardService

↓

ExportPDFService

Cada Service possuirá apenas uma responsabilidade.

---

# 17. DTOs

AnalysisRequestDTO (com lista de repositórios)

IssueDTO (com repositório de origem)

PullRequestDTO

MetricDTO

PRMetricsDTO

RepositorySummaryDTO

DashboardDTO

InsightDTO

---

# 18. Tratamento de Erros

GitHub indisponível

↓

Mensagem amigável

↓

Dashboard vazio

---

Erro IA

↓

Ignorar resumo

↓

Apresentar métricas normalmente

---

# 19. Segurança

Nesta primeira versão será utilizado:

GitHub Personal Access Token

armazenado em

application.yml

---

# 20. Logging

Todos os passos principais deverão ser registrados.

Exemplos

Consulta GitHub

Início da IA

Tempo de processamento

Fim da análise

---

# 21. Testes

## Unitários

MetricsService (issues + PRs)

PRMetricsService

GitHubMapper

DashboardService

InsightService

ExportPDFService

---

## Integração

GitHub

Spring AI

LangGraph

---

# 22. Padrões

- SOLID

- Clean Code

- Clean Architecture

- DTO Pattern

- Dependency Injection

- Strategy

- Builder

---

# 23. Convenções

Classes

PascalCase

Métodos

camelCase

Constantes

UPPER_CASE

Pacotes

lowercase

---

# 24. Evolução

A arquitetura foi planejada para permitir:

- múltiplos usuários

- Jira

- Azure DevOps

- GitLab

- histórico

- métricas DORA

- dashboards comparativos

sem necessidade de alterar a arquitetura principal.

---

# 25. Decisões Arquiteturais (ADRs)

## ADR-001

Utilizar GitHub REST API por simplicidade no MVP.

---

## ADR-002

Utilizar LangGraph4j para orquestrar o fluxo da aplicação, facilitando futuras expansões do agente.

---

## ADR-003

Separar o cálculo de métricas da geração de insights para manter baixo acoplamento.

---

## ADR-004

A geração de insights por IA é opcional. Caso a IA esteja indisponível, o dashboard continua funcional.

---

## ADR-005

A exportação de PDF será feita via Flying Saucer (Java) por não exigir dependência externa como Puppeteer.
Caso a renderização de gráficos SVG seja problemática, migrar para Thymeleaf + Puppeteer via subprocesso.

---

## ADR-006

O multi-repo adiciona o campo `repositories` ao `AnalysisRequest`. GitHubClient deve aceitar lista e iterar sobre cada repositório.
Issues e PRs de cada repo recebem o identificador `repository` para agregação posterior.

---

## ADR-007

As métricas de PR são calculadas em serviço separado (`PRMetricsService`) para não acoplar com a lógica de issues.
O `DashboardReport` agrega ambos os resultados.

---

# 27. Roadmap Técnico

## Versão Atual

- Integração GitHub (Issues + PRs)
- Metrics Engine (issues + PRs)
- Multi-Repo Support
- Dashboard com seções Issues e PR Analytics
- Exportação PDF
- Spring AI com contexto multi-repo
- LangGraph4j

## Próxima versão

- Persistência em banco de dados
- Histórico de análises
- Dashboards comparativos entre períodos

## Futuro

- Jira
- Azure DevOps
- GitLab
- DORA Metrics
- Dashboards comparativos entre desenvolvedores