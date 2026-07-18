package com.devreport.ai;

import com.devreport.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsightService {

    private static final Logger log = LoggerFactory.getLogger(InsightService.class);

    private final ChatClient chatClient;
    private final IssueSelector issueSelector;
    private final boolean mockMode;

    public InsightService(ChatClient chatClient, IssueSelector issueSelector,
                          @Value("${devreport.mock-mode:false}") boolean mockMode) {
        this.chatClient = chatClient;
        this.issueSelector = issueSelector;
        this.mockMode = mockMode;
    }

    public Insight generateInsight(Metric metrics, List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        return generateInsight(metrics, null, null, 0, issues, null, startDate, endDate);
    }

    public Insight generateInsight(Metric metrics, PRMetrics prMetrics,
                                    List<RepositorySummary> repositorySummaries, int repositoriesCount,
                                    List<Issue> issues, List<PullRequest> pullRequests,
                                    LocalDate startDate, LocalDate endDate) {
        if (metrics == null || metrics.getTotal() == 0) {
            log.info("No metrics to generate insight from");
            return null;
        }

        // In mock mode, generate a fallback summary directly (no API key needed)
        if (mockMode) {
            log.info("Mock mode: generating fallback AI insight from metrics");
            return new Insight(buildFallbackSummary(metrics, prMetrics, repositoriesCount,
                    startDate, endDate));
        }

        String topIssues = issueSelector.selectTopIssues(issues);
        String topPRs = issueSelector.selectTopPRs(pullRequests);

        String prompt = buildPrompt(metrics, prMetrics, repositorySummaries, repositoriesCount,
                topIssues, topPRs, startDate, endDate);

        try {
            log.info("Requesting AI insight generation");
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                log.warn("AI returned empty response, using fallback summary");
                return new Insight(buildFallbackSummary(metrics, prMetrics, repositoriesCount,
                        startDate, endDate));
            }

            log.info("AI insight generated successfully ({} chars)", response.length());
            return new Insight(response);

        } catch (Exception e) {
            log.error("Failed to generate AI insight: {}. Using fallback summary.", e.getMessage());
            return new Insight(buildFallbackSummary(metrics, prMetrics, repositoriesCount,
                    startDate, endDate));
        }
    }

    private String buildPrompt(Metric metrics, PRMetrics prMetrics,
                                List<RepositorySummary> repositorySummaries, int repositoriesCount,
                                String topIssues, String topPRs,
                                LocalDate startDate, LocalDate endDate) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("""
                O usuário realizou %d entregas (issues) em %d repositórios entre %s e %s.

                Distribuição de issues:
                - Features: %d
                - Bugs: %d
                - Tasks: %d

                """,
                metrics.getTotal(), repositoriesCount, startDate, endDate,
                metrics.getFeatures(), metrics.getBugs(), metrics.getTasks()));

        // QW metrics - always include if available
        if (metrics.getThroughputPerWeek() > 0) {
            sb.append(String.format("Média de %.1f entregas por semana.\n",
                    metrics.getThroughputPerWeek()));
        }
        if (metrics.getAvgResolutionDays() > 0) {
            sb.append(String.format("Tempo médio de resolução: %.1f dias.\n",
                    metrics.getAvgResolutionDays()));
        }
        if (metrics.getBugDensityPercent() > 0) {
            sb.append(String.format("Densidade de bugs: %.1f%% das entregas foram correções.\n\n",
                    metrics.getBugDensityPercent()));
        } else {
            sb.append("\n");
        }

        if (prMetrics != null && prMetrics.getTotalMerged() > 0) {
            sb.append(String.format("""
                    Pull Requests no período:
                    - PRs merged: %d
                    - Linhas alteradas: +%d / -%d (%d arquivos)
                    - Tempo médio até merge: %.1f horas
                    - Revisores distintos: %d
                    - Cobertura de code review: %.1f%%

                    """,
                    prMetrics.getTotalMerged(),
                    prMetrics.getTotalAdditions(), prMetrics.getTotalDeletions(),
                    prMetrics.getTotalChangedFiles(),
                    prMetrics.getAverageTimeToMerge(),
                    prMetrics.getUniqueReviewers(),
                    prMetrics.getReviewCoveragePercent()));
        }

        if (repositorySummaries != null && !repositorySummaries.isEmpty()) {
            sb.append("Repositórios:\n");
            for (RepositorySummary rs : repositorySummaries) {
                sb.append(String.format("- %s: %d issues, %d PRs, +%d linhas\n",
                        rs.getName(), rs.getTotalIssues(), rs.getTotalPRs(), rs.getTotalAdditions()));
            }
            sb.append("\n");
        }

        if (topIssues != null && !topIssues.isBlank()) {
            sb.append("Principais issues:\n").append(topIssues).append("\n\n");
        }

        if (topPRs != null && !topPRs.isBlank()) {
            sb.append("Principais PRs:\n").append(topPRs).append("\n\n");
        }

        sb.append("""
                Você é um analista de desempenho que prepara um resumo para apoiar uma conversa de reconhecimento profissional.
                Seu objetivo é apresentar os dados de forma que as contribuições de valor do desenvolvedor fiquem evidentes,
                com um tom profissional, positivo e convincente — sem parecer que está pedindo algo diretamente.

                Formate a resposta em HTML simples, usando as seguintes seções:

                <div class="summary-section highlight">
                <h3>🎯 Destaques do Período</h3>
                <p>Uma frase de abertura impactante que resuma o valor entregue. Inclua o volume total de entregas e o throughput semanal, contextualizando o ritmo de trabalho. Ex: "Foram X entregas concluídas com um ritmo consistente de Y por semana, demonstrando disciplina e previsibilidade nas entregas."</p>
                </div>

                <div class="summary-section metrics">
                <h3>📊 Métricas de Impacto</h3>
                <ul>
                <li><strong>Velocidade de Resolução:</strong> destaque o tempo médio de resolução, enquadrando como agilidade na entrega de valor</li>
                <li><strong>Qualidade:</strong> analise a densidade de bugs — se baixa, destaque a qualidade do código; se alta, enquadre como proatividade em corrigir problemas</li>
                <li><strong>PRs e Colaboração:</strong> mencione total de PRs, linhas alteradas, revisores distintos e cobertura de review, mostrando colaboração com o time</li>
                <li><strong>Equilíbrio:</strong> comente a distribuição entre features, bugs e tasks, mostrando versatilidade</li>
                </ul>
                </div>

                <div class="summary-section value">
                <h3>💡 Valor para o Time</h3>
                <p>Uma frase que conecte os números ao impacto no time/projeto. Destaque constância, qualidade, colaboração e o fato de que as entregas mostram um profissional que vai além do básico. O tom deve ser natural, como se estivesse descrevendo fatos, não fazendo um pedido.</p>
                </div>

                Regras importantes:
                - Use EXATAMENTE a estrutura HTML acima (as 3 divs com as classes indicadas)
                - Substitua os textos de exemplo pelos dados reais
                - Se PR metrics não estiverem disponíveis, omita o bullet de PRs ou adapte
                - Escreva em português do Brasil
                - Tom profissional, confiante e baseado em dados
                - NÃO use markdown (sem ** ou *), use as tags HTML fornecidas
                - NÃO invente métricas, use apenas os dados fornecidos
                - Máximo 200 palavras no total""");

        return sb.toString();
    }

    /**
     * Generates a data-driven fallback summary when AI is unavailable (e.g., mock mode or API failure).
     * Uses the same HTML structure as the AI prompt so the dashboard renders consistently.
     */
    private String buildFallbackSummary(Metric metrics, PRMetrics prMetrics,
                                         int repositoriesCount,
                                         LocalDate startDate, LocalDate endDate) {
        int total = metrics.getTotal();
        int features = metrics.getFeatures();
        int bugs = metrics.getBugs();
        int tasks = metrics.getTasks();
        double throughput = metrics.getThroughputPerWeek();
        double avgResolution = metrics.getAvgResolutionDays();
        double bugDensity = metrics.getBugDensityPercent();

        StringBuilder sb = new StringBuilder();

        // === Highlight Section ===
        sb.append("<div class=\"summary-section highlight\">\n");
        sb.append("<h3>🎯 Destaques do Período</h3>\n");
        sb.append("<p>");
        sb.append(String.format(
                "Foram <strong>%d entregas</strong> concluídas em <strong>%d %s</strong> ",
                total, repositoriesCount,
                repositoriesCount == 1 ? "repositório" : "repositórios"));
        sb.append(String.format("entre %s e %s", startDate, endDate));
        if (throughput > 0) {
            sb.append(String.format(", com um ritmo consistente de <strong>%.1f entregas por semana</strong>", throughput));
        }
        sb.append(", demonstrando disciplina e previsibilidade nas entregas.");
        sb.append("</p>\n");
        sb.append("</div>\n");

        // === Metrics Section ===
        sb.append("<div class=\"summary-section metrics\">\n");
        sb.append("<h3>📊 Métricas de Impacto</h3>\n");
        sb.append("<ul>\n");

        // Resolution speed
        if (avgResolution > 0) {
            String speedDesc = avgResolution < 1 ? "excelente agilidade" :
                    avgResolution < 3 ? "ótima agilidade" : "boa agilidade";
            sb.append(String.format(
                    "<li><strong>Velocidade de Resolução:</strong> tempo médio de <strong>%.1f dias</strong> para resolver cada issue — %s na entrega de valor.</li>\n",
                    avgResolution, speedDesc));
        }

        // Quality
        if (bugDensity > 0) {
            String qualityDesc = bugDensity < 10 ? "código de qualidade" :
                    bugDensity < 25 ? "atenção a melhorias" : "proatividade em corrigir problemas";
            sb.append(String.format(
                    "<li><strong>Qualidade:</strong> densidade de bugs de <strong>%.1f%%</strong> — %s.</li>\n",
                    bugDensity, qualityDesc));
        }

        // PRs and Collaboration
        if (prMetrics != null && prMetrics.getTotalMerged() > 0) {
            sb.append(String.format(
                    "<li><strong>PRs e Colaboração:</strong> <strong>%d PRs</strong> merged, ",
                    prMetrics.getTotalMerged()));
            sb.append(String.format(
                    "+%d / -%d linhas em %d arquivos, ",
                    prMetrics.getTotalAdditions(), prMetrics.getTotalDeletions(),
                    prMetrics.getTotalChangedFiles()));
            sb.append(String.format(
                    "com <strong>%d revisores</strong> distintos e %.1f%% de cobertura de code review, ",
                    prMetrics.getUniqueReviewers(), prMetrics.getReviewCoveragePercent()));
            sb.append("mostrando colaboração ativa com o time.</li>\n");
        }

        // Balance
        sb.append("<li><strong>Equilíbrio:</strong> ");
        sb.append(String.format(
                "%d features, %d bugs e %d tasks ", features, bugs, tasks));
        if (features > 0 && features >= (bugs + tasks)) {
            sb.append("— foco em entregar valor com features, mantendo atenção a correções e tarefas de sustentação.");
        } else if (bugs > features) {
            sb.append("— forte atuação em correções, essencial para a estabilidade do projeto.");
        } else {
            sb.append("— distribuição versátil entre entregas de valor, correções e sustentação.");
        }
        sb.append("</li>\n");

        sb.append("</ul>\n");
        sb.append("</div>\n");

        // === Value Section ===
        sb.append("<div class=\"summary-section value\">\n");
        sb.append("<h3>💡 Valor para o Time</h3>\n");
        sb.append("<p>");

        StringBuilder valueText = new StringBuilder();
        valueText.append(String.format("Com %d entregas no período", total));
        if (throughput > 0) {
            valueText.append(String.format(" e uma média de %.1f por semana", throughput));
        }
        valueText.append(", o trabalho demonstra constância e comprometimento. ");
        if (prMetrics != null && prMetrics.getTotalMerged() > 0 && prMetrics.getUniqueReviewers() > 1) {
            valueText.append(String.format(
                    "A colaboração com %d revisores distintos reforça o impacto positivo no time. ",
                    prMetrics.getUniqueReviewers()));
        }
        valueText.append("As métricas confirmam um profissional que entrega valor de forma consistente e colaborativa.");
        sb.append(valueText.toString());

        sb.append("</p>\n");
        sb.append("</div>\n");

        return sb.toString();
    }
}
