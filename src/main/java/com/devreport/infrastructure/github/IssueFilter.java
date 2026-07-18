package com.devreport.infrastructure.github;

import com.devreport.domain.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Component
public class IssueFilter {

    private static final Logger log = LoggerFactory.getLogger(IssueFilter.class);

    public List<Issue> filterByPeriod(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        if (issues == null || issues.isEmpty()) {
            return Collections.emptyList();
        }

        if (startDate == null || endDate == null) {
            log.warn("Filter period with null dates: start={}, end={}", startDate, endDate);
            return Collections.emptyList();
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Issue> filtered = issues.stream()
                .filter(issue -> issue.getClosedAt() != null)
                .filter(issue -> !issue.getClosedAt().isBefore(start))
                .filter(issue -> !issue.getClosedAt().isAfter(end))
                .toList();

        log.info("Filtered {} issues by period [{}, {}]: {} remaining",
                issues.size(), startDate, endDate, filtered.size());

        return filtered;
    }
}
