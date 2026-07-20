package com.devreport.infrastructure.github;

import com.devreport.domain.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GitHubIssueMapper {

    private static final Logger log = LoggerFactory.getLogger(GitHubIssueMapper.class);
    private static final DateTimeFormatter GITHUB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final ObjectMapper objectMapper;

    public GitHubIssueMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Issue> mapToIssues(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<Issue> issues = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode node : root) {
                    mapSingleIssue(node).ifPresent(issues::add);
                }
            }

            log.info("Mapped {} issues from GitHub response", issues.size());
            return issues;

        } catch (Exception e) {
            String preview = jsonResponse.length() > 300
                    ? jsonResponse.substring(0, 300) + "..."
                    : jsonResponse;
            log.error("Failed to parse GitHub response ({} chars): {} — preview: {}",
                    jsonResponse.length(), e.getMessage(), preview);
            return Collections.emptyList();
        }
    }

    private java.util.Optional<Issue> mapSingleIssue(JsonNode node) {
        try {
            String id = node.has("id") && !node.get("id").isNull()
                    ? node.get("id").asText() : null;
            String title = node.has("title") && !node.get("title").isNull()
                    ? node.get("title").asText() : null;
            String description = node.has("body") && !node.get("body").isNull()
                    ? node.get("body").asText() : null;

            List<String> labels = extractLabels(node.get("labels"));

            LocalDateTime createdAt = null;
            if (node.has("created_at") && !node.get("created_at").isNull()) {
                createdAt = LocalDateTime.parse(node.get("created_at").asText(), GITHUB_DATE_FORMAT);
            }

            LocalDateTime closedAt = null;
            if (node.has("closed_at") && !node.get("closed_at").isNull()) {
                closedAt = LocalDateTime.parse(node.get("closed_at").asText(), GITHUB_DATE_FORMAT);
            }

            String author = null;
            if (node.has("user") && !node.get("user").isNull() && node.get("user").has("login")) {
                author = node.get("user").get("login").asText();
            }

            String project = null;
            // Projects v2 info not available in standard issues endpoint

            String repository = null;
            if (node.has("repository") && !node.get("repository").isNull()) {
                repository = node.get("repository").asText();
            }

            if (id == null || title == null || closedAt == null) {
                log.warn("Skipping issue with missing required fields: id={}, title={}, closedAt={}", id, title, closedAt);
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(new Issue(id, title, description, labels, createdAt, closedAt, author, project, repository));

        } catch (Exception e) {
            log.warn("Failed to map a single issue node: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    private List<String> extractLabels(JsonNode labelsNode) {
        if (labelsNode == null || !labelsNode.isArray()) {
            return Collections.emptyList();
        }

        List<String> labels = new ArrayList<>();
        for (JsonNode labelNode : labelsNode) {
            if (labelNode.has("name")) {
                labels.add(labelNode.get("name").asText());
            }
        }
        return labels;
    }
}
