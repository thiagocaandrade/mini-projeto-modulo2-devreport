$env:GITHUB_TOKEN = $env:GITHUB_TOKEN  # set externally
$owner = "IA-para-DEVs-SCTEC-T2"
$repo = "mini-projeto-modulo2-devreport"
$projectNumber = 58
$headers = @{
    Authorization = "Bearer $env:GITHUB_TOKEN"
    Accept = "application/vnd.github.v3+json"
}
$apiBase = "https://api.github.com"

# Get project ID first
$orgProjectsUrl = "$apiBase/orgs/$owner/projects"
$projects = Invoke-RestMethod -Uri $orgProjectsUrl -Headers $headers
$project = $projects | Where-Object { $_.number -eq $projectNumber }
$projectId = $project.id
Write-Host "Project ID: $projectId (Number: $projectNumber)"

# All 58 tasks extracted from 06-tasks.md
$tasks = @(
    @{num="001"; prio="P0"; spec="SPEC-001"; desc="Criar ou ajustar a base do projeto Spring Boot usando Java 21 e Maven."; result="Aplicação Spring Boot inicial compilável."; deps="Nenhuma."; verify="O projeto possui `pom.xml`. A aplicação possui classe principal Spring Boot. O build Maven executa sem erro."},
    @{num="002"; prio="P0"; spec="SPEC-001"; desc="Adicionar dependências necessárias para Spring Web, Thymeleaf, validação, Spring AI, LangGraph4j, cliente HTTP, testes e suporte ao frontend."; result="Dependências essenciais disponíveis no build."; deps="TASK-001."; verify="Maven resolve dependências. A aplicação inicia sem erro de classe ausente. Dependências de teste estão disponíveis."},
    @{num="003"; prio="P0"; spec="SPEC-001"; desc="Criar pacotes `config`, `controller`, `application`, `domain`, `infrastructure`, `github`, `metrics`, `dashboard`, `ai`, `agent` e `shared`."; result="Estrutura de pacotes alinhada ao TDD."; deps="TASK-001."; verify="Pacotes existem em `src/main/java/br/com/devreport`. Responsabilidades de código seguem a separação planejada."},
    @{num="004"; prio="P0"; spec="SPEC-001"; desc="Criar `AnalysisRequest`, `Issue`, `Metric`, `DashboardReport`, `Insight` e modelos auxiliares para dados de gráficos."; result="Domínio mínimo para representar análise, entregas, métricas, insight e relatório."; deps="TASK-003."; verify="Modelos possuem campos mínimos definidos nas Specifications. Modelos de domínio não dependem de Spring. Código compila."},
    @{num="005"; prio="P0"; spec="SPEC-001"; desc="Criar `AnalysisRequestDTO`, `IssueDTO`, `MetricDTO`, `DashboardDTO` e `InsightDTO`."; result="Contratos de entrada e saída para controller/view."; deps="TASK-004."; verify="DTOs existem e representam os dados necessários. DTOs não substituem regras de domínio."},
    @{num="006"; prio="P0"; spec="SPEC-002"; desc="Criar propriedades para token, owner, repository e project em `application.yml` ou configuração equivalente."; result="Configuração GitHub carregável pela aplicação."; deps="TASK-002."; verify="Propriedades são lidas por classe de configuração. Token não é impresso em logs. Configuração inválida gera erro controlável."},
    @{num="007"; prio="P0"; spec="SPEC-002"; desc="Criar view Thymeleaf inicial com campos `startDate` e `endDate`."; result="Usuário consegue informar período para análise."; deps="TASK-002, TASK-005."; verify="Tela renderiza no navegador. Campos de data estão presentes. Formulário envia dados ao backend."},
    @{num="008"; prio="P0"; spec="SPEC-002"; desc="Validar campos obrigatórios e impedir data final anterior à inicial."; result="Entrada inválida não inicia análise."; deps="TASK-007."; verify="Datas ausentes exibem validação. Data final anterior à inicial exibe validação. Período válido segue para análise."},
    @{num="009"; prio="P0"; spec="SPEC-002"; desc="Criar `DashboardController` para exibir tela inicial e receber solicitação de análise."; result="Controller conecta formulário ao caso de uso/fluxo de análise."; deps="TASK-005, TASK-007, TASK-008."; verify="`GET` renderiza dashboard inicial. `POST` recebe período informado. Controller não contém regra de negócio."},
    @{num="010"; prio="P0"; spec="SPEC-003"; desc="Criar componente de infraestrutura para chamadas à GitHub REST API usando token configurado."; result="Cliente capaz de realizar requisições autenticadas ao GitHub."; deps="TASK-006."; verify="Cabeçalho de autenticação é enviado. URL base e parâmetros usam configuração. Token não aparece em logs."},
    @{num="011"; prio="P0"; spec="SPEC-003"; desc="Implementar serviço para buscar issues concluídas do repositório/projeto configurado."; result="Lista bruta de issues concluídas recuperada da API."; deps="TASK-010."; verify="Consulta retorna issues fechadas. Issues abertas não são consideradas. Falha HTTP é capturada para tratamento posterior."},
    @{num="012"; prio="P0"; spec="SPEC-003"; desc="Converter resposta da API GitHub para o modelo `Issue` do domínio."; result="Dados externos normalizados para uso interno."; deps="TASK-004, TASK-011."; verify="Campos obrigatórios são mapeados. Campos opcionais ausentes não quebram conversão. Labels são preservadas para classificação."},
    @{num="013"; prio="P0"; spec="SPEC-003"; desc="Filtrar issues por `closedAt` dentro de `startDate` e `endDate`."; result="Apenas entregas do período informado seguem para análise."; deps="TASK-008, TASK-012."; verify="Issues antes do período são removidas. Issues depois do período são removidas. Issues sem `closedAt` são removidas."},
    @{num="014"; prio="P1"; spec="SPEC-003"; desc="Padronizar erros de autenticação, indisponibilidade e falhas de comunicação com GitHub."; result="Erros externos viram respostas tratáveis pelo fluxo principal."; deps="TASK-011."; verify="Erro 401/403 não expõe token. Erro de rede gera exceção ou resultado controlado. Mensagem final é amigável."},
    @{num="015"; prio="P0"; spec="SPEC-004"; desc="Classificar cada issue como Feature, Bug ou Task a partir de labels, com fallback para Task."; result="Cada issue possui uma categoria única."; deps="TASK-012, TASK-013."; verify="Label feature classifica como Feature. Label bug classifica como Bug. Label task classifica como Task. Label desconhecida ou ausente classifica como Task."},
    @{num="016"; prio="P0"; spec="SPEC-004"; desc="Calcular total, features, bugs e tasks a partir da lista classificada."; result="`Metric` preenchida corretamente."; deps="TASK-015."; verify="Total corresponde à quantidade de issues filtradas. Soma das categorias equivale ao total. Lista vazia retorna zeros."},
    @{num="017"; prio="P0"; spec="SPEC-004"; desc="Agregar entregas por período para consumo pelo Chart.js."; result="Estrutura com labels e valores por período."; deps="TASK-016."; verify="Issues são agrupadas por data ou período definido. Saída é serializável para a view. Lista vazia retorna estrutura vazia."},
    @{num="018"; prio="P0"; spec="SPEC-004"; desc="Criar estrutura de dados para gráfico de distribuição por Feature, Bug e Task."; result="Dados de categoria prontos para Chart.js."; deps="TASK-016."; verify="Saída contém Feature, Bug e Task. Valores batem com `Metric`. Lista vazia retorna zeros."},
    @{num="019"; prio="P0"; spec="SPEC-005"; desc="Criar estado compartilhado do agente com datas, issues, métricas, resumo, dashboard e erros."; result="`AnalysisState` usado pelos nodes do fluxo."; deps="TASK-004."; verify="Estado possui campos definidos na SPEC-005. Estado permite leitura e atualização pelos nodes."},
    @{num="020"; prio="P0"; spec="SPEC-005"; desc="Implementar `StartNode`, `ValidateRequestNode`, `FetchGitHubDataNode`, `CalculateMetricsNode`, `GenerateInsightsNode` e `BuildDashboardNode`."; result="Nodes isolados por responsabilidade."; deps="TASK-013, TASK-016, TASK-019."; verify="Cada node executa apenas sua responsabilidade. Nodes atualizam `AnalysisState`. Validação impede consulta GitHub em entrada inválida."},
    @{num="021"; prio="P0"; spec="SPEC-005"; desc="Conectar nodes no fluxo LangGraph4j na ordem definida pela Specification."; result="Agente executa análise de ponta a ponta."; deps="TASK-020."; verify="Ordem do fluxo é validação, GitHub, métricas, IA e dashboard. Métricas são calculadas antes da IA. Erro de IA não bloqueia dashboard."},
    @{num="022"; prio="P0"; spec="SPEC-005"; desc="Fazer `DashboardController` acionar o agente e receber `DashboardReport`."; result="Formulário dispara fluxo real da aplicação."; deps="TASK-009, TASK-021."; verify="Solicitação válida aciona agente. Resultado é enviado para a view. Erros são enviados para apresentação amigável."},
    @{num="023"; prio="P0"; spec="SPEC-006"; desc="Implementar `DashboardService` para consolidar métricas, gráficos, resumo e mensagens."; result="Relatório final unificado para a view."; deps="TASK-017, TASK-018, TASK-019."; verify="`DashboardReport` contém métricas. `DashboardReport` contém dados de gráficos. `DashboardReport` aceita resumo opcional."},
    @{num="024"; prio="P0"; spec="SPEC-006"; desc="Criar template base do dashboard com Bootstrap 5."; result="Tela web estruturada e responsiva."; deps="TASK-007, TASK-023."; verify="Template renderiza sem erro. Layout mantém formulário e área de relatório. Interface é utilizável em desktop."},
    @{num="025"; prio="P0"; spec="SPEC-006"; desc="Exibir cards de Total de Entregas, Features, Bugs e Tasks."; result="Usuário visualiza indicadores principais."; deps="TASK-016, TASK-024."; verify="Cards exibem valores do backend. Valores zerados são exibidos corretamente. Layout não quebra em telas menores."},
    @{num="026"; prio="P0"; spec="SPEC-006"; desc="Adicionar Chart.js ao template e preparar scripts para renderização."; result="Dashboard pronto para renderizar gráficos."; deps="TASK-024."; verify="Chart.js carrega na tela. Scripts não geram erro no navegador. Dados podem ser injetados pela view."},
    @{num="027"; prio="P0"; spec="SPEC-006"; desc="Exibir gráfico de entregas por período com dados calculados pelo backend."; result="Gráfico temporal visível no dashboard."; deps="TASK-017, TASK-026."; verify="Gráfico usa labels e valores reais. Cenário sem dados não quebra renderização. Gráfico é legível no layout."},
    @{num="028"; prio="P0"; spec="SPEC-006"; desc="Exibir gráfico de distribuição por Feature, Bug e Task."; result="Gráfico de categorias visível no dashboard."; deps="TASK-018, TASK-026."; verify="Gráfico usa dados reais de categoria. Valores batem com cards. Cenário sem dados não quebra renderização."},
    @{num="029"; prio="P1"; spec="SPEC-006"; desc="Criar área no dashboard para exibir insight gerado por IA ou mensagem de ausência controlada."; result="Resumo aparece junto aos indicadores quando disponível."; deps="TASK-024."; verify="Texto de resumo é exibido quando presente. Ausência de resumo não quebra layout. Mensagem de ausência é amigável."},
    @{num="030"; prio="P1"; spec="SPEC-007"; desc="Configurar dependências, propriedades e client do Spring AI para chamada ao modelo OpenAI GPT."; result="Infraestrutura de IA pronta para uso pelo `InsightService`."; deps="TASK-002."; verify="Propriedades de IA são carregadas. Credenciais não são expostas em logs. Serviço pode ser simulado em testes."},
    @{num="031"; prio="P1"; spec="SPEC-007"; desc="Selecionar subconjunto representativo de issues para alimentar o prompt."; result="Lista limitada de entregas relevantes para IA."; deps="TASK-013, TASK-016."; verify="Seleção inclui título, descrição e categoria quando disponíveis. Quantidade enviada ao prompt é limitada. Lista vazia é tratada."},
    @{num="032"; prio="P1"; spec="SPEC-007"; desc="Criar `InsightService` com prompt baseado em métricas e principais entregas."; result="Serviço retorna `Insight` com resumo executivo."; deps="TASK-030, TASK-031."; verify="Prompt contém total, categorias e entregas. Resumo é retornado quando IA responde. Erro da IA é capturado."},
    @{num="033"; prio="P1"; spec="SPEC-007"; desc="Conectar `InsightService` ao `GenerateInsightsNode` e exibir resultado no dashboard."; result="IA participa do fluxo sem bloquear métricas."; deps="TASK-021, TASK-029, TASK-032."; verify="Insight é gerado após métricas. Dashboard exibe o resumo. Falha de IA mantém cards e gráficos."},
    @{num="034"; prio="P1"; spec="SPEC-008"; desc="Exibir estado vazio quando não houver issues concluídas no período."; result="Usuário entende que a consulta funcionou, mas não encontrou entregas."; deps="TASK-013, TASK-023, TASK-024."; verify="Mensagem de ausência de entregas aparece. Cards e gráficos não quebram. Estado vazio é distinto de erro GitHub."},
    @{num="035"; prio="P1"; spec="SPEC-008"; desc="Exibir mensagem amigável quando GitHub estiver indisponível, token inválido ou configuração incorreta."; result="Falhas GitHub não expõem detalhes sensíveis."; deps="TASK-014, TASK-022, TASK-024."; verify="Mensagem amigável é exibida. Token não aparece na resposta. Dashboard permanece acessível."},
    @{num="036"; prio="P1"; spec="SPEC-008"; desc="Tornar erro da IA não bloqueante e exibir dashboard com métricas."; result="Dashboard funciona mesmo sem resumo inteligente."; deps="TASK-032, TASK-033."; verify="Erro de IA não interrompe fluxo. Cards e gráficos continuam visíveis. Mensagem de resumo indisponível é exibida quando aplicável."},
    @{num="037"; prio="P1"; spec="SPEC-008"; desc="Registrar início/fim da análise, consulta GitHub, cálculo de métricas, IA e tempo total."; result="Logs úteis para diagnóstico do MVP."; deps="TASK-021, TASK-033, TASK-035."; verify="Logs cobrem os principais passos. Tempo de processamento é registrado. Logs não expõem token ou segredos."},
    @{num="038"; prio="P1"; spec="SPEC-009"; desc="Testar totalização, categorias, lista vazia e consistência da soma."; result="Motor de métricas validado."; deps="TASK-015, TASK-016."; verify="Testes cobrem Feature, Bug e Task. Teste cobre fallback para Task. Testes passam localmente."},
    @{num="039"; prio="P1"; spec="SPEC-009"; desc="Testar conversão de dados GitHub para domínio e categorização."; result="Entrada externa validada antes de alimentar métricas."; deps="TASK-012, TASK-015."; verify="Campos obrigatórios são mapeados. Campos opcionais ausentes são aceitos. Classificação por labels é consistente."},
    @{num="040"; prio="P1"; spec="SPEC-009"; desc="Testar montagem do `DashboardReport` e comportamento do `InsightService` com sucesso e falha."; result="Relatório e IA possuem cobertura mínima."; deps="TASK-023, TASK-032, TASK-036."; verify="Dashboard report contém métricas e gráficos. Resumo opcional é tratado. Falha da IA é coberta por teste."},
    @{num="041"; prio="P2"; spec="SPEC-009"; desc="Criar teste do fluxo principal com GitHub e IA simulados."; result="Validação de ponta a ponta sem dependências externas reais."; deps="TASK-021, TASK-033, TASK-035, TASK-036."; verify="Período válido com issues simuladas retorna dashboard. Fluxo sem dados retorna estado vazio. Falha de IA mantém métricas. Falha de GitHub retorna erro amigável."},
    @{num="042"; prio="P0"; spec="SPEC-010"; desc="Criar endpoint `GET /api/repositories` no GitHubClient para listar repositórios do owner configurado via GitHub REST API."; result="Frontend pode popular campo de seleção de repositórios."; deps="TASK-010."; verify="Endpoint retorna lista de repositórios disponíveis. A listagem respeita o token configurado. A resposta é serializável para consumo pelo frontend."},
    @{num="043"; prio="P0"; spec="SPEC-010"; desc="Adicionar campo multi-select ou checkboxes no formulário Thymeleaf, populado dinamicamente via endpoint de listagem de repositórios."; result="Usuário pode selecionar um ou mais repositórios antes de gerar o relatório."; deps="TASK-007, TASK-042."; verify="Formulário exibe lista de repositórios disponíveis. Usuário pode selecionar múltiplos. Repositórios selecionados são enviados ao backend."},
    @{num="044"; prio="P0"; spec="SPEC-010"; desc="Modificar GitHubClient para aceitar lista de repositórios e iterar sobre cada um nas consultas de issues e PRs."; result="Cliente coleta dados de N repositórios em uma única análise."; deps="TASK-011, TASK-047."; verify="Cliente aceita lista de repositórios. Para cada repositório, issues e PRs são coletados. Cada item recebe o identificador do repositório de origem. Falha em um repositório não interrompe os demais."},
    @{num="045"; prio="P1"; spec="SPEC-010"; desc="Garantir que cada Issue e PullRequest contenha o campo `repository` (owner/repo) identificando sua origem, e que as listas sejam agregadas corretamente."; result="Dados de múltiplos repositórios são unificados com rastreabilidade de origem."; deps="TASK-044."; verify="Toda Issue possui `repository` preenchido. Todo PullRequest possui `repository` preenchido. Listas agregadas mantêm a ordem de coleta."},
    @{num="046"; prio="P1"; spec="SPEC-010"; desc="Calcular indicadores por repositório: total de issues, total de PRs e total de additions."; result="Dashboard pode exibir tabela/resumo por repositório."; deps="TASK-016, TASK-045, TASK-049."; verify="RepositorySummary é calculado para cada repositório. Totais por repositório são consistentes com o agregado. Lista vazia retorna summaries vazios."},
    @{num="047"; prio="P1"; spec="SPEC-011"; desc="Implementar consulta à GitHub REST API para recuperar PRs merged do período, incluindo additions, deletions, changedFiles, revisores e comentários."; result="Lista de PRs merged disponível para métricas."; deps="TASK-010, TASK-044."; verify="Apenas PRs merged são retornados. PRs fora do período não são incluídos. Dados de additions, deletions, changedFiles e revisores são coletados."},
    @{num="048"; prio="P1"; spec="SPEC-011"; desc="Criar mapper para converter resposta da API GitHub de PRs para o modelo `PullRequest` do domínio."; result="Dados de PR normalizados para uso interno."; deps="TASK-047."; verify="Campos obrigatórios são mapeados. Lista de revisores é extraída corretamente. Campos opcionais ausentes não quebram conversão."},
    @{num="049"; prio="P1"; spec="SPEC-011"; desc="Implementar serviço para calcular totalMerged, additions, deletions, changedFiles, averageTimeToMerge, uniqueReviewers e prSizeDistribution."; result="PRMetrics disponível para o dashboard."; deps="TASK-048."; verify="totalMerged reflete quantidade de PRs. Linhas alteradas = additions + deletions. Tempo até merge é calculado em horas. Revisores distintos são contados. Distribuição small/medium/large é calculada. Lista vazia retorna métricas zeradas."},
    @{num="050"; prio="P1"; spec="SPEC-011"; desc="Adicionar seção 'PR Analytics' no dashboard Thymeleaf com cards de PRs merged, linhas alteradas, tempo até merge e revisores."; result="Usuário visualiza métricas de PR no dashboard."; deps="TASK-024, TASK-049."; verify="Cards de PR aparecem no dashboard. Valores refletem métricas calculadas. Seção fica visível apenas quando há dados de PR. Layout não quebra com ou sem PRs."},
    @{num="051"; prio="P1"; spec="SPEC-011"; desc="Adicionar gráfico Chart.js de distribuição por tamanho de PR (pequeno/médio/grande)."; result="Gráfico de PR visível no dashboard."; deps="TASK-026, TASK-049, TASK-050."; verify="Gráfico usa dados de distribuição de PR. Valores batem com os cards. Cenário sem dados não quebra renderização."},
    @{num="052"; prio="P1"; spec="SPEC-011"; desc="Modificar o prompt da IA para incluir métricas de PR (merged, linhas, tempo) e contexto multi-repo (quantidade de repositórios, destaques individuais)."; result="Resumo da IA menciona dados de PR e múltiplos repositórios."; deps="TASK-032, TASK-046, TASK-049."; verify="Prompt inclui PRMetrics e RepositorySummary. Resumo menciona quantidade de repositórios. Resumo pode destacar repositório com maior contribuição."},
    @{num="053"; prio="P1"; spec="SPEC-012"; desc="Criar endpoint `GET /dashboard/export` que aceita período e repositórios, reutiliza o fluxo de análise e retorna PDF."; result="Usuário pode baixar relatório em PDF."; deps="TASK-009, TASK-054, TASK-055."; verify="Endpoint aceita mesmos parâmetros do dashboard. Resposta tem Content-Type `application/pdf`. Download é disparado no navegador. PDF contém dados corretos do período."},
    @{num="054"; prio="P1"; spec="SPEC-012"; desc="Criar template específico para PDF com CSS `@media print`, dimensões A4, gráficos SVG inline, cabeçalho com período/repositórios, cards KPI e resumo IA."; result="Template renderiza HTML próprio para conversão em PDF."; deps="TASK-024, TASK-050."; verify="Template possui CSS `@media print`. Dimensões seguem A4. Gráficos são SVG inline. Elementos interativos são ocultados. Layout não quebra entre páginas."},
    @{num="055"; prio="P1"; spec="SPEC-012"; desc="Implementar serviço que renderiza template Thymeleaf e converte para PDF usando Flying Saucer (ou biblioteca equivalente)."; result="HTML é convertido em PDF válido."; deps="TASK-054."; verify="HTML é renderizado com Thymeleaf. Conversão produz PDF sem erros. PDF pode ser aberto em leitores comuns. Acentos e caracteres especiais são preservados."},
    @{num="056"; prio="P1"; spec="SPEC-010"; desc="Testar que falha em um repositório durante coleta multi-repo não interrompe a análise dos demais."; result="Resiliência da consulta multi-repo validada."; deps="TASK-044."; verify="Um repositório simulado com falha não impede coleta dos outros. Log registra falha do repositório específico. Dashboard exibe mensagem sobre falha parcial."},
    @{num="057"; prio="P1"; spec="SPEC-011"; desc="Testar cálculo de PR metrics: merged total, additions/deletions, tempo até merge, revisores e distribuição de tamanho."; result="PRMetricsService validado unitariamente."; deps="TASK-049."; verify="Teste cobre PRs com múltiplos revisores. Teste cobre PRs sem revisores. Teste cobre distribuição small/medium/large. Teste cobre lista vazia. Tempo até merge é calculado corretamente."},
    @{num="058"; prio="P2"; spec="SPEC-009"; desc="Criar teste de integração simulando múltiplos repositórios com issues e PRs, validando agregação, métricas de PR e exportação PDF."; result="Fluxo completo (multi-repo + issues + PRs + PDF) validado sem dependências externas."; deps="TASK-041, TASK-045, TASK-052, TASK-055."; verify="Múltiplos repositórios com issues e PRs retornam dashboard consolidado. RepositorySummary reflete cada repositório individualmente. Métricas de PR são consistentes. PDF é gerado sem erro. Falha em um repositório mantém análise dos demais."},
    @{num="059"; prio="P1"; spec="SPEC-008"; desc="Consolidar correções finais do MVP: remover BOM de arquivos Java, implementar modo mock no InsightService para funcionamento sem chave API OpenAI, consolidar dependências no pom.xml, refinar templates dashboard e PDF, e ajustar controller/DTOs."; result="Projeto compila e executa sem erros de encoding; IA funciona em modo mock sem depender de API externa; templates renderizam corretamente."; deps="TASK-032, TASK-036, TASK-055."; verify="Arquivos Java não possuem BOM. Projeto compila com mvnw. Modo mock da IA gera resumo fallback. Templates dashboard e PDF renderizam sem erros. Dependências no pom.xml estão consolidadas."}
)

$total = $tasks.Count
$created = 0
$failed = @()

foreach ($task in $tasks) {
    $num = $task.num
    Write-Host "[$num/$total] Creating TASK-$num ..."
    
    $title = "TASK-$($num): $($task.desc.Substring(0, [Math]::Min(80, $task.desc.Length)))"
    if ($task.desc.Length -gt 80) { $title += "..." }
    
    $body = @"
## TASK-$num

**Prioridade:** $($task.prio)
**Specification:** $($task.spec)

### Descrição
$($task.desc)

### Resultado Esperado
$($task.result)

### Dependências
$($task.deps)

### Verificação
$($task.verify)
"@

    $issueBody = @{
        title = $title
        body = $body
        labels = @($task.prio.ToLower(), "task")
    } | ConvertTo-Json -Depth 3

    try {
        $createUrl = "$apiBase/repos/$owner/$repo/issues"
        $issue = Invoke-RestMethod -Uri $createUrl -Method Post -Headers $headers -Body $issueBody -ContentType "application/json"
        
        # Close the issue immediately so it has closed_at
        $closeUrl = "$apiBase/repos/$owner/$repo/issues/$($issue.number)"
        $closeBody = @{ state = "closed" } | ConvertTo-Json
        Invoke-RestMethod -Uri $closeUrl -Method Patch -Headers $headers -Body $closeBody -ContentType "application/json" | Out-Null
        
        # Add to project
        try {
            # Get project node ID for GraphQL
            $gqlHeaders = @{
                Authorization = "Bearer $env:GITHUB_TOKEN"
                Accept = "application/vnd.github.v3+json"
            }
            $addProjectBody = @{
                query = "mutation { addProjectV2ItemById(input: {projectId: `"$projectId`" contentId: `"$($issue.node_id)`"}) { item { id } } }"
            } | ConvertTo-Json -Compress
            
            # Actually using REST API for project
            $projectUrl = "$apiBase/projects/columns?per_page=10"
            # Skip project linking for now - issues will be linked via repo
        } catch {
            Write-Host "  (project link skipped)"
        }
        
        Write-Host "  OK #$($issue.number) - $($issue.html_url)"
        $created++
        Start-Sleep -Milliseconds 500  # Rate limit safety
    } catch {
        $errMsg = $_.Exception.Message
        if ($errMsg.Length -gt 150) { $errMsg = $errMsg.Substring(0, 150) }
        Write-Host "  FAILED: $errMsg"
        $failed += "TASK-$num"
    }
}

Write-Host "`n=== DONE ==="
Write-Host "Created: $created / $total"
if ($failed.Count -gt 0) {
    Write-Host "FAILED: $($failed -join ', ')"
}
