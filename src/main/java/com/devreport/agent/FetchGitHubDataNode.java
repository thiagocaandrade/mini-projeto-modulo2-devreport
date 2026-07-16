package com.devreport.agent;

import com.devreport.domain.Issue;
import com.devreport.domain.PullRequest;
import com.devreport.infrastructure.github.GitHubIssueService;
import com.devreport.infrastructure.github.GitHubIssueMapper;
import com.devreport.infrastructure.github.GitHubPRService;
import com.devreport.infrastructure.github.GitHubPRMapper;
import com.devreport.infrastructure.github.IssueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FetchGitHubDataNode {

    private static final Logger log = LoggerFactory.getLogger(FetchGitHubDataNode.class);

    private final GitHubIssueService issueService;
    private final GitHubIssueMapper issueMapper;
    private final IssueFilter issueFilter;
    private final GitHubPRService prService;
    private final GitHubPRMapper prMapper;

    public FetchGitHubDataNode(GitHubIssueService issueService,
                                GitHubIssueMapper issueMapper,
                                IssueFilter issueFilter,
                                GitHubPRService prService,
                                GitHubPRMapper prMapper) {
        this.issueService = issueService;
        this.issueMapper = issueMapper;
        this.issueFilter = issueFilter;
        this.prService = prService;
        this.prMapper = prMapper;
    }

    public AnalysisState execute(AnalysisState state) {
        log.info("Fetching GitHub data for period: {} to {}", state.getStartDate(), state.getEndDate());

        List<String> repositories = state.getRepositories();

        // Fetch issues
        try {
            String rawIssuesJson;
            if (repositories != null && !repositories.isEmpty()) {
                rawIssuesJson = issueService.fetchClosedIssuesRaw(repositories);
            } else {
                rawIssuesJson = issueService.fetchClosedIssuesRaw();
            }
            List<Issue> allIssues = issueMapper.mapToIssues(rawIssuesJson);
            List<Issue> filtered = issueFilter.filterByPeriod(allIssues, state.getStartDate(), state.getEndDate());
            state.setIssues(filtered);

            if (filtered.isEmpty()) {
                state.setMessage("Não existem entregas concluídas para o período informado.");
                log.info("No issues found for the period");
            } else {
                log.info("Found {} issues for the period", filtered.size());
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub issues: {}", e.getMessage());
            state.setMessage("Não foi possível consultar os dados do GitHub no momento.");
            state.getErrors().add("GitHub fetch failed: " + e.getMessage());
        }

        // Fetch PRs (non-blocking - errors don't stop issue metrics)
        try {
            String rawPRsJson;
            if (repositories != null && !repositories.isEmpty()) {
                rawPRsJson = prService.fetchMergedPRsRaw(repositories);
            } else {
                rawPRsJson = prService.fetchMergedPRsRaw();
            }
            List<PullRequest> allPRs = prMapper.mapToPullRequests(rawPRsJson);
            List<PullRequest> filteredPRs = filterPRsByPeriod(allPRs, state.getStartDate(), state.getEndDate());
            state.setPullRequests(filteredPRs);
            log.info("Found {} merged PRs for the period", filteredPRs.size());
        } catch (Exception e) {
            log.error("Failed to fetch GitHub PRs: {}", e.getMessage());
            state.setPullRequests(new ArrayList<>());
            // Non-blocking: don't set error, just log
        }

        return state;
    }

    private List<PullRequest> filterPRsByPeriod(List<PullRequest> prs, java.time.LocalDate start, java.time.LocalDate end) {
        if (prs == null || prs.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();

        return prs.stream()
                .filter(pr -> pr.getMergedAt() != null)
                .filter(pr -> !pr.getMergedAt().isBefore(startDateTime) && pr.getMergedAt().isBefore(endDateTime))
                .toList();
    }

    public boolean hasErrors(AnalysisState state) {
        return !state.getErrors().isEmpty();
    }
}
