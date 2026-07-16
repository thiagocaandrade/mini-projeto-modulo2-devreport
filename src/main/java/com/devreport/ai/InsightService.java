package com.devreport.ai;

import com.devreport.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsightService {

    private static final Logger log = LoggerFactory.getLogger(InsightService.class);

    private final ChatClient chatClient;
    private final IssueSelector issueSelector;

    public InsightService(ChatClient chatClient, IssueSelector issueSelector) {
        this.chatClient = chatClient;
        this.issueSelector = issueSelector;
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
                log.warn("AI returned empty response");
                return null;
            }

            log.info("AI insight generated successfully ({} chars)", response.length());
            return new Insight(response);

        } catch (Exception e) {
            log.error("Failed to generate AI insight: {}", e.getMessage());
            return null;
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
                Crie um resumo executivo objetivo para apoiar uma reunião de feedback profissional.
                Destaque o repositório com maior contribuição se aplicável.
                Destaque pontos positivos, equilíbrio entre tipos de entrega e sugira pontos de atenção
                se houver muitos bugs ou PRs muito grandes. O tom deve ser profissional e construtivo.
                Escreva em português do Brasil, em um único parágrafo de 3 a 5 frases.""");

        return sb.toString();
    }
}
