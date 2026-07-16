# Project Brief
**Projeto:** DevReport

**Versão:** 1.0

**Data:** Julho/2026

**Autor:** Thiago Carlos Andrade

---

# 1. Visão Geral

O DevReport é uma aplicação web que transforma dados de tarefas concluídas no GitHub Projects em indicadores de desempenho profissionais.

O objetivo é permitir que desenvolvedores acompanhem suas entregas de forma objetiva, apresentando métricas, gráficos e insights gerados por Inteligência Artificial.

Na primeira versão, o sistema permitirá consultar tarefas concluídas em um ou múltiplos repositórios, calcular indicadores de produtividade, analisar Pull Requests merged, exportar relatórios em PDF e apresentar um dashboard interativo com um resumo executivo gerado por IA.

---

# 2. Problema

Durante avaliações de desempenho, feedbacks individuais e solicitações de promoção ou aumento salarial, é comum que desenvolvedores tenham dificuldade em demonstrar objetivamente o valor entregue ao longo do tempo.

Embora ferramentas como GitHub Projects armazenem todas as tarefas realizadas, essas informações permanecem dispersas e pouco acessíveis para análises gerenciais.

Consequentemente:

- não existe uma visão consolidada das entregas;
- métricas precisam ser levantadas manualmente;
- feedbacks dependem da memória do colaborador ou do gestor;
- torna-se difícil demonstrar evolução profissional baseada em dados.

---

# 3. Objetivo

Desenvolver uma aplicação capaz de:

- consultar automaticamente tarefas concluídas no GitHub Projects;
- consultar Pull Requests merged por período;
- suportar múltiplos repositórios em uma única análise;
- calcular métricas relevantes de produtividade (issues e PRs);
- apresentar gráficos e indicadores;
- gerar um resumo inteligente utilizando IA;
- exportar relatório em PDF formatado;
- disponibilizar um dashboard simples para acompanhamento das entregas.

---

# 4. Público-Alvo

O MVP é destinado principalmente para desenvolvedores de software que desejam acompanhar sua evolução profissional.

Futuramente o sistema poderá atender:

- Tech Leads
- Engineering Managers
- Scrum Masters
- Product Managers
- Equipes de Engenharia

---

# 5. Proposta de Valor

O DevReport transforma dados operacionais do GitHub em informações estratégicas para apoiar:

- avaliações de desempenho;
- reuniões 1:1;
- feedbacks periódicos;
- promoções;
- planejamento de carreira.

Ao invés de analisar dezenas de tarefas individualmente, o usuário passa a visualizar indicadores claros e um resumo inteligente sobre seu período de trabalho.

---

# 6. Escopo da Primeira Entrega (MVP)

A primeira versão contempla apenas funcionalidades essenciais.

## Consulta de dados

- conectar ao GitHub utilizando Personal Access Token;
- consultar GitHub Projects e Issues;
- recuperar Issues concluídas;
- recuperar Pull Requests merged;
- filtrar por período;
- **selecionar um ou múltiplos repositórios dinamicamente.**

## Processamento

- calcular quantidade de entregas;
- calcular quantidade de Features, Bugs e Tasks;
- calcular distribuição por período;
- **calcular métricas de PR: merged, linhas alteradas, tempo até merge, tamanho, revisores;**
- **agregar métricas de múltiplos repositórios em um relatório unificado.**

## Inteligência Artificial

Utilizando Spring AI:

- gerar resumo executivo;
- identificar principais entregas;
- produzir insights sobre produtividade;
- **incorporar métricas de PR e contexto multi-repo no resumo.**

## Dashboard

Interface Web utilizando Thymeleaf.

Apresentar:

- Total de Entregas
- Features, Bugs, Tasks
- **Seção de PR Analytics (PRs merged, linhas alteradas, ciclo, revisores)**
- **Indicador de repositórios analisados**
- Gráfico por período
- Gráfico de distribuição por categoria
- Resumo Inteligente
- **Botão "Baixar Relatório PDF"**

---

# 7. Fora do Escopo

Nesta primeira versão não serão implementadas funcionalidades como:

- autenticação de usuários;
- múltiplos usuários;
- integração com Jira ou Azure DevOps;
- comparação entre desenvolvedores;
- histórico de análises persistido em banco;
- métricas avançadas de DORA;
- banco de dados;
- controle de permissões.

Essas funcionalidades poderão fazer parte das próximas versões.

---

# 8. Tecnologias

Backend

- Java 21
- Spring Boot 3
- Spring AI
- LangGraph4j

Frontend

- Thymeleaf
- Bootstrap
- Chart.js

Integrações

- GitHub REST API

IA

- OpenAI GPT

---

# 9. Arquitetura Proposta

A solução seguirá uma arquitetura em camadas baseada em Clean Architecture.

O fluxo principal será:

Usuário

↓

LangGraph4j

↓

GitHub API

↓

Metrics Engine

↓

Spring AI

↓

Dashboard

O LangGraph4j será responsável por coordenar toda a execução do agente.

---

# 10. Critérios de Sucesso

O MVP será considerado concluído quando for possível:

- conectar ao GitHub;
- consultar tarefas concluídas;
- filtrar por período;
- calcular métricas automaticamente;
- visualizar gráficos;
- gerar um resumo inteligente utilizando IA.

---

# 11. Roadmap

## Versão 1 (atual)

- Integração GitHub (Issues + PRs)
- Multi-Repo Support
- Dashboard com métricas e gráficos
- PR Analytics
- Exportação PDF
- Resumo Inteligente com IA
- LangGraph4j

## Versão 2

- Histórico de análises
- Dashboard Comparativo entre períodos
- Comparação entre desenvolvedores

## Versão 3

- Integração Jira
- Integração Azure DevOps
- Métricas DORA
- Recomendações Inteligentes
- Benchmark de produtividade

---

# 12. Restrições

Este projeto possui caráter acadêmico.

O desenvolvimento deverá ser concluído em aproximadamente três dias.

Por esse motivo, serão priorizadas funcionalidades que demonstrem claramente:

- integração com serviços externos;
- utilização de IA Generativa;
- arquitetura moderna;
- aplicação de LangGraph4j;
- boas práticas de engenharia de software.

---

# 13. Resultado Esperado

Ao final do projeto, o usuário poderá realizar uma pergunta como:

> "Como foram minhas entregas entre 01/01/2026 e 14/07/2026?"

O sistema responderá apresentando:

- indicadores de produtividade (issues e PRs);
- gráficos;
- distribuição das entregas;
- métricas de Pull Requests (tempo até merge, linhas alteradas, revisores);
- resumo executivo gerado por IA com contexto multi-repo;
- opção de exportar o relatório completo em PDF.

Essas informações poderão ser utilizadas como apoio em reuniões de feedback, avaliações de desempenho e planejamento de carreira.