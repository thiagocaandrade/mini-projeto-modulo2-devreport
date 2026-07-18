package com.devreport.infrastructure.github;

import com.devreport.domain.PullRequest;
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
public class GitHubPRMapper {

    private static final Logger log = LoggerFactory.getLogger(GitHubPRMapper.class);
    private static final DateTimeFormatter GITHUB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final ObjectMapper objectMapper;

    public GitHubPRMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<PullRequest> mapToPullRequests(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<PullRequest> pullRequests = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode node : root) {
                    mapSinglePR(node).ifPresent(pullRequests::add);
                }
            }

            log.info("Mapped {} pull requests from GitHub response", pullRequests.size());
            return pullRequests;

        } catch (Exception e) {
            log.error("Failed to parse GitHub PR response: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private java.util.Optional<PullRequest> mapSinglePR(JsonNode node) {
        try {
            Long id = node.has("id") && !node.get("id").isNull()
                    ? node.get("id").asLong() : null;
            int number = node.has("number") && !node.get("number").isNull()
                    ? node.get("number").asInt() : 0;
            String title = node.has("title") && !node.get("title").isNull()
                    ? node.get("title").asText() : null;

            LocalDateTime createdAt = null;
            if (node.has("created_at") && !node.get("created_at").isNull()) {
                createdAt = LocalDateTime.parse(node.get("created_at").asText(), GITHUB_DATE_FORMAT);
            }

            LocalDateTime mergedAt = null;
            if (node.has("merged_at") && !node.get("merged_at").isNull()) {
                mergedAt = LocalDateTime.parse(node.get("merged_at").asText(), GITHUB_DATE_FORMAT);
            }

            int additions = node.has("additions") && !node.get("additions").isNull()
                    ? node.get("additions").asInt() : 0;
            int deletions = node.has("deletions") && !node.get("deletions").isNull()
                    ? node.get("deletions").asInt() : 0;
            int changedFiles = node.has("changed_files") && !node.get("changed_files").isNull()
                    ? node.get("changed_files").asInt() : 0;

            List<String> reviewers = extractReviewers(node.get("requested_reviewers"));
            int reviewComments = node.has("review_comments") && !node.get("review_comments").isNull()
                    ? node.get("review_comments").asInt() : 0;

            List<String> labels = extractLabels(node.get("labels"));

            String repository = node.has("repository") && !node.get("repository").isNull()
                    ? node.get("repository").asText() : null;

            if (id == null || title == null || mergedAt == null) {
                log.warn("Skipping PR with missing required fields: id={}, title={}, mergedAt={}", id, title, mergedAt);
                return java.util.Optional.empty();
            }

            return java.util.Optional.of(new PullRequest(
                    id, number, title, createdAt, mergedAt,
                    additions, deletions, changedFiles,
                    reviewers, reviewComments, labels, repository));

        } catch (Exception e) {
            log.warn("Failed to map a single PR node: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    private List<String> extractReviewers(JsonNode reviewersNode) {
        if (reviewersNode == null || !reviewersNode.isArray()) {
            return Collections.emptyList();
        }

        List<String> reviewers = new ArrayList<>();
        for (JsonNode reviewerNode : reviewersNode) {
            if (reviewerNode.has("login")) {
                reviewers.add(reviewerNode.get("login").asText());
            }
        }
        return reviewers;
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
