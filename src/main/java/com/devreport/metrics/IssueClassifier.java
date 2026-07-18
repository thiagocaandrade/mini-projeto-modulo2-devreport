package com.devreport.metrics;

import com.devreport.domain.Issue;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class IssueClassifier {

    public IssueCategory classify(Issue issue) {
        List<String> labels = issue.getLabels();
        if (labels == null || labels.isEmpty()) {
            return IssueCategory.TASK;
        }
        for (String label : labels) {
            String lower = label.toLowerCase();
            if (lower.contains("feature")) {
                return IssueCategory.FEATURE;
            }
            if (lower.contains("bug")) {
                return IssueCategory.BUG;
            }
            if (lower.contains("task")) {
                return IssueCategory.TASK;
            }
        }
        return IssueCategory.TASK;
    }
}
