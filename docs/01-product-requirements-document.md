# Product Requirements Document (PRD)

# DevReport

Versão: 1.0

Data: Julho/2026

Autor: Thiago Carlos Andrade

---

# 1. Objetivo

O DevReport é uma aplicação que transforma dados de tarefas concluídas no GitHub Projects em indicadores de desempenho profissionais.

O sistema permitirá que desenvolvedores consultem suas entregas em um determinado período, visualizem métricas consolidadas e obtenham um resumo executivo gerado por Inteligência Artificial.

O objetivo principal é facilitar reuniões de feedback, avaliações de desempenho e acompanhamento da evolução profissional.

---

# 2. Problema

Atualmente os desenvolvedores possuem dezenas ou centenas de tarefas distribuídas no GitHub Projects.

Embora essas informações existam, elas não são apresentadas de maneira consolidada.

Isso gera diversos problemas:

- dificuldade para demonstrar resultados;
- preparação manual para avaliações de desempenho;
- pouca visibilidade sobre produtividade;
- ausência de indicadores históricos.

---

# 3. Objetivos do Produto

O sistema deverá permitir que o usuário:

- consulte tarefas concluídas;
- filtre entregas por período;
- visualize indicadores de produtividade;
- visualize gráficos;
- obtenha insights utilizando IA.

---

# 4. Público-Alvo

Primário

- Desenvolvedores

Secundário

- Tech Leads
- Engineering Managers
- Scrum Masters

---

# 5. Escopo do MVP

O MVP deverá atender apenas um único usuário.

Não haverá autenticação.

O sistema permitirá consultar dados de **um ou múltiplos repositórios** GitHub, selecionados dinamicamente pelo usuário.

---

# 6. Personas

## Desenvolvedor

Objetivo

Acompanhar sua produtividade e preparar reuniões de feedback.

Necessidades

- visualizar entregas;
- acompanhar evolução;
- gerar evidências objetivas.

---

## Gestor

Objetivo

Compreender rapidamente as entregas realizadas por um colaborador.

Necessidades

- indicadores claros;
- gráficos;
- resumo executivo.

---

# 7. Casos de Uso

## UC01

Consultar entregas.

---

## UC02

Filtrar entregas por período.

---

## UC03

Visualizar dashboard.

---

## UC04

Visualizar métricas.

---

## UC05

Gerar resumo inteligente.

---

## UC06

Selecionar múltiplos repositórios para análise.

---

## UC07

Visualizar métricas de Pull Requests.

---

## UC08

Exportar relatório em PDF.

---

# 8. Fluxo Principal

1. Usuário informa período e seleciona um ou mais repositórios.

2. Sistema consulta issues e PRs para cada repositório.

3. Sistema agrega dados e calcula métricas (issues + PRs).

4. Sistema gera insights com IA.

5. Dashboard é apresentado com seções de Issues e PR Analytics.

6. Usuário pode exportar o relatório em PDF.

---

# 9. Fluxos Alternativos

## GitHub indisponível

O sistema deverá informar que não foi possível consultar os dados.

---

## Nenhuma tarefa encontrada

O dashboard deverá informar que não existem entregas para o período informado.

---

## Erro na IA

As métricas deverão continuar sendo exibidas.

O resumo inteligente poderá ser omitido.

---

# 10. Requisitos Funcionais

## RF01

Permitir informar um período inicial e final.

---

## RF02

Consultar tarefas concluídas no GitHub.

---

## RF03

Recuperar informações das Issues.

---

## RF04

Agrupar entregas por tipo.

Exemplos

- Feature

- Bug

- Task

---

## RF05

Calcular quantidade total de entregas.

---

## RF06

Calcular quantidade por categoria.

---

## RF07

Apresentar gráficos.

---

## RF08

Gerar resumo inteligente.

---

## RF09

Exibir dashboard consolidado.

---

## RF10

Selecionar múltiplos repositórios para compor a análise.

---

## RF11

Consultar Pull Requests merged no período.

---

## RF12

Calcular métricas de PR: total merged, linhas alteradas, tempo até merge, revisores, tamanho.

---

## RF13

Agregar métricas de issues e PRs de múltiplos repositórios em um relatório unificado.

---

## RF14

Exportar relatório completo em PDF.

---

# 11. Requisitos Não Funcionais

## RNF01

A aplicação deverá possuir interface Web responsiva.

---

## RNF02

Tempo médio de carregamento inferior a cinco segundos.

---

## RNF03

Código organizado utilizando boas práticas.

---

## RNF04

Arquitetura preparada para evolução.

---

## RNF05

Separação clara entre regras de negócio e infraestrutura.

---

# 12. Regras de Negócio

## RN01

Somente tarefas concluídas deverão ser consideradas.

---

## RN02

Somente tarefas pertencentes ao período informado deverão ser analisadas.

---

## RN03

Cada Issue deverá pertencer apenas a uma categoria.

---

## RN04

As métricas deverão ser calculadas antes da geração do resumo por IA.

---

## RN05

Caso a IA esteja indisponível, o dashboard continuará funcionando normalmente.

---

# 13. Dashboard

O dashboard deverá apresentar:

**Seção Issues**

Card

- Total de Entregas

Card

- Features

Card

- Bugs

Card

- Tasks

Gráfico

- Entregas por período

Gráfico

- Distribuição por categoria

**Seção PR Analytics**

Card

- PRs merged

Card

- Linhas alteradas (additions + deletions)

Card

- Tempo médio até merge

Card

- Total de revisores

Gráfico

- Distribuição por tamanho de PR (pequeno/médio/grande)

**Indicador de repositórios analisados**

- "3 repositórios analisados neste período"

**Resumo**

- Insights da IA (incluindo contexto multi-repo e métricas de PR)

**Ações**

- Botão "📄 Baixar Relatório PDF"

---

# 14. Dados Necessários

**Issues** - cada entrega deverá possuir, quando disponível:

- título

- descrição

- tipo

- labels

- data de conclusão

- responsável

- projeto

- repositório de origem

**Pull Requests** - cada PR deverá possuir, quando disponível:

- título

- número

- data de criação

- data de merge

- additions

- deletions

- arquivos modificados

- revisores (login)

- comentários de review

- labels

- repositório de origem

---

# 15. Critérios de Aceite

## Consulta

Dado um período válido

Quando o usuário solicitar o relatório

Então o sistema deverá apresentar todas as tarefas concluídas.

---

## Métricas

Dado um conjunto de tarefas

Quando a análise for executada

Então o sistema deverá calcular corretamente todas as métricas.

---

## Dashboard

Dado que existam entregas

Quando o processamento terminar

Então o dashboard deverá apresentar gráficos e indicadores.

---

## IA

Dado que existam métricas

Quando a IA for executada

Então deverá ser produzido um resumo executivo.

---

## Múltiplos Repositórios

Dado que o usuário selecione N repositórios

Quando a análise for executada

Então as issues e PRs de todos os repositórios deverão ser agregados no mesmo relatório.

---

## PR Analytics

Dado que existam PRs merged no período

Quando o dashboard for exibido

Então as métricas de PR deverão ser apresentadas na seção específica.

---

## Exportação PDF

Dado que o dashboard esteja carregado

Quando o usuário clicar em baixar relatório

Então o sistema deverá retornar um arquivo PDF formatado com todos os dados do dashboard.

---

# 16. Fora do Escopo

Não fazem parte desta entrega:

- login;

- múltiplos usuários;

- persistência em banco de dados;

- integração Jira;

- integração Azure DevOps;

- comparação entre colaboradores;

- métricas DORA;

- notificações.

---

# 17. Roadmap

## Versão Atual

- Consulta GitHub (Issues + PRs)

- Multi-Repo Support

- Dashboard com métricas e gráficos

- PR Analytics

- Exportação PDF

- IA com contexto multi-repo

---

## Próxima versão

- Histórico de análises

- Comparação entre períodos

- Dashboard executivo

---

## Futuro

- Jira

- Azure DevOps

- GitLab

- Métricas DORA

- Recomendações inteligentes

---

# 18. Métricas de Sucesso

O projeto será considerado bem-sucedido quando permitir:

- consultar entregas em poucos segundos;

- apresentar indicadores consistentes;

- gerar gráficos automaticamente;

- produzir um resumo inteligente utilizando IA;

- apoiar reuniões de feedback com informações objetivas.