package com.devreport.metrics;

import com.devreport.domain.Issue;
import com.devreport.domain.Metric;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MetricsService {

    private final IssueClassifier classifier;

    public MetricsService(IssueClassifier classifier) {
        this.classifier = classifier;
    }

    public Metric calculate(List<Issue> issues) {
        return calculate(issues, null, null);
    }

    public Metric calculate(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        if (issues == null || issues.isEmpty()) {
            return new Metric(0, 0, 0, 0);
        }
        int features = 0, bugs = 0, tasks = 0;
        double totalResolutionDays = 0.0;
        int issuesWithDates = 0;

        for (Issue issue : issues) {
            IssueCategory category = classifier.classify(issue);
            switch (category) {
                case FEATURE -> features++;
                case BUG -> bugs++;
                case TASK -> tasks++;
            }

            // Calculate resolution velocity: days between createdAt and closedAt
            if (issue.getCreatedAt() != null && issue.getClosedAt() != null) {
                double days = ChronoUnit.HOURS.between(issue.getCreatedAt(), issue.getClosedAt()) / 24.0;
                if (days >= 0) {
                    totalResolutionDays += days;
                    issuesWithDates++;
                }
            }
        }

        int total = features + bugs + tasks;

        // QW-1: Throughput per week
        double throughputPerWeek = 0.0;
        if (startDate != null && endDate != null) {
            long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            double weeks = totalDays / 7.0;
            if (weeks > 0) {
                throughputPerWeek = total / weeks;
            }
        }

        // QW-2: Average resolution velocity in days
        double avgResolutionDays = issuesWithDates > 0
                ? totalResolutionDays / issuesWithDates
                : 0.0;

        // QW-4: Bug density percentage
        double bugDensityPercent = total > 0 ? (bugs * 100.0 / total) : 0.0;

        return new Metric(total, features, bugs, tasks,
                throughputPerWeek, avgResolutionDays, bugDensityPercent);
    }
}
