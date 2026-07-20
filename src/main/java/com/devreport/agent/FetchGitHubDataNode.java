package com.devreport.agent;

import com.devreport.domain.Issue;
import com.devreport.domain.PullRequest;
import com.devreport.infrastructure.github.GitHubIssueService;
import com.devreport.infrastructure.github.GitHubIssueMapper;
import com.devreport.infrastructure.github.GitHubPRService;
import com.devreport.infrastructure.github.GitHubPRMapper;
import com.devreport.infrastructure.github.IssueFilter;
import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FetchGitHubDataNode implements NodeAction<AnalysisState> {

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

    @Override
    public Map<String, Object> apply(AnalysisState state) {
        log.info("Fetching GitHub data for period: {} to {}", state.getStartDate(), state.getEndDate());

        List<String> repositories = state.getRepositories();
        Map<String, Object> result = new LinkedHashMap<>();
        List<Issue> filteredIssues = new ArrayList<>();
        List<PullRequest> filteredPRs = new ArrayList<>();
        List<String> errors = new ArrayList<>(state.getErrors());

        // Fetch issues
        try {
            String rawIssuesJson;
            if (repositories != null && !repositories.isEmpty()) {
                rawIssuesJson = issueService.fetchClosedIssuesRaw(repositories);
            } else {
                rawIssuesJson = issueService.fetchClosedIssuesRaw();
            }
            List<Issue> allIssues = issueMapper.mapToIssues(rawIssuesJson);
            filteredIssues = issueFilter.filterByPeriod(allIssues, state.getStartDate(), state.getEndDate());

            if (filteredIssues.isEmpty()) {
                result.put(AnalysisState.MESSAGE_KEY, "Não existem entregas concluídas para o período informado.");
                log.warn("No issues found. Raw fetched: {}, after filter: 0", allIssues.size());
            } else {
                log.info("Found {} issues for the period", filteredIssues.size());
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub issues: {}", e.getMessage());
            result.put(AnalysisState.MESSAGE_KEY, "Não foi possível consultar os dados do GitHub no momento.");
            errors.add("GitHub fetch failed: " + e.getMessage());
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
            filteredPRs = filterPRsByPeriod(allPRs, state.getStartDate(), state.getEndDate());
            log.info("Found {} merged PRs for the period", filteredPRs.size());
        } catch (Exception e) {
            log.error("Failed to fetch GitHub PRs: {}", e.getMessage());
            // Non-blocking: don't set error, just log
        }

        result.put(AnalysisState.ISSUES_KEY, filteredIssues);
        result.put(AnalysisState.PULL_REQUESTS_KEY, filteredPRs);
        if (!errors.isEmpty()) {
            result.put(AnalysisState.ERRORS_KEY, errors);
        }
        return result;
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
