package com.devreport.ai;

import com.devreport.domain.Issue;
import com.devreport.domain.PullRequest;
import com.devreport.metrics.IssueCategory;
import com.devreport.metrics.IssueClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class IssueSelector {

    private static final Logger log = LoggerFactory.getLogger(IssueSelector.class);
    private static final int MAX_ISSUES = 10;
    private static final int MAX_DESC_LENGTH = 150;
    private static final int MAX_TOTAL_CHARS = 2000;

    private final IssueClassifier classifier;

    public IssueSelector(IssueClassifier classifier) {
        this.classifier = classifier;
    }

    public String selectTopIssues(List<Issue> issues) {
        if (issues == null || issues.isEmpty()) {
            return "Nenhuma entrega no período.";
        }

        List<Issue> sorted = issues.stream()
                .sorted(Comparator.comparingInt(this::categoryPriority))
                .limit(MAX_ISSUES)
                .toList();

        StringBuilder sb = new StringBuilder();
        for (Issue issue : sorted) {
            IssueCategory category = classifier.classify(issue);
            String desc = issue.getDescription() != null ? issue.getDescription() : "";
            if (desc.length() > MAX_DESC_LENGTH) {
                desc = desc.substring(0, MAX_DESC_LENGTH) + "...";
            }
            String line = String.format("- [%s] %s: %s%n", category, issue.getTitle(), desc);
            if (sb.length() + line.length() > MAX_TOTAL_CHARS) {
                break;
            }
            sb.append(line);
        }

        log.info("Selected {} issues for AI prompt ({} chars)",
                sb.toString().split("\n").length, sb.length());
        return sb.toString();
    }

    private int categoryPriority(Issue issue) {
        return switch (classifier.classify(issue)) {
            case FEATURE -> 0;
            case BUG -> 1;
            case TASK -> 2;
        };
    }

    public String selectTopPRs(List<PullRequest> pullRequests) {
        if (pullRequests == null || pullRequests.isEmpty()) {
            return "";
        }

        List<PullRequest> sorted = pullRequests.stream()
                .sorted(Comparator.comparingInt(PullRequest::getAdditions).reversed())
                .limit(5)
                .toList();

        StringBuilder sb = new StringBuilder();
        for (PullRequest pr : sorted) {
            String repo = pr.getRepository() != null ? " (" + pr.getRepository() + ")" : "";
            String line = String.format("- PR #%d: %s [+%d/-%d]%s%n",
                    pr.getNumber(), pr.getTitle(),
                    pr.getAdditions(), pr.getDeletions(), repo);
            sb.append(line);
        }

        return sb.toString();
    }
}
