package com.devreport.infrastructure.github;

import com.devreport.config.GitHubProperties;
import com.devreport.domain.Issue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GitHubProjectService {

    private static final Logger log = LoggerFactory.getLogger(GitHubProjectService.class);
    private static final DateTimeFormatter GITHUB_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final int PER_PAGE = 100;
    private static final int MAX_PAGES = 5;

    private final GitHubClient gitHubClient;
    private final GitHubProperties properties;
    private final ObjectMapper objectMapper;

    public GitHubProjectService(GitHubClient gitHubClient, GitHubProperties properties, ObjectMapper objectMapper) {
        this.gitHubClient = gitHubClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetch issues from a GitHub Project V2 using the REST Search API.
     * Uses the search qualifier "org" and "assignee" to filter issues.
     * Project association is checked via the search API's project qualifier when available.
     */
    @SuppressWarnings("unchecked")
    public List<Issue> fetchProjectIssues(String owner, String repo) {
        String assignee = properties.getAssignee();
        int projectNumber = parseProjectNumber();

        log.info("Fetching Project V2 #{} issues via REST Search for org={}, assignee={}",
                projectNumber, owner, assignee);

        // Build search query using GitHub REST Search API qualifiers
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("org:").append(owner);
        queryBuilder.append(" assignee:").append(assignee);
        queryBuilder.append(" is:issue");
        queryBuilder.append(" sort:updated-desc");

        String searchQuery = queryBuilder.toString();
        log.debug("Search query: {}", searchQuery);

        List<Issue> allIssues = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String response = gitHubClient.searchIssues(searchQuery, page, PER_PAGE);

                if (response == null || response.isEmpty()) {
                    break;
                }

                Map<String, Object> searchResult = objectMapper.readValue(response, Map.class);

                List<Map<String, Object>> items = (List<Map<String, Object>>) searchResult.get("items");
                if (items == null || items.isEmpty()) {
                    break;
                }

                for (Map<String, Object> item : items) {
                    Issue issue = mapSearchItemToIssue(item, owner, repo);
                    if (issue != null) {
                        allIssues.add(issue);
                    }
                }

                // Check if there are more pages
                int totalCount = ((Number) searchResult.getOrDefault("total_count", 0)).intValue();
                if (page * PER_PAGE >= totalCount) {
                    break;
                }

            } catch (RuntimeException e) {
                log.error("Search API failed at page {}: {}", page, e.getMessage());
                break;
            } catch (Exception e) {
                log.error("Failed to parse search results at page {}: {}", page, e.getMessage());
                break;
            }
        }

        log.info("Found {} issues via REST Search for org={}, assignee={}",
                allIssues.size(), owner, assignee);
        return allIssues;
    }

    @SuppressWarnings("unchecked")
    private Issue mapSearchItemToIssue(Map<String, Object> item, String owner, String repo) {
        try {
            // Search API returns: number, title, body, state, closed_at, created_at, html_url,
            // labels (array of {name, color, ...}), assignee, repository_url, etc.
            Object numberObj = item.get("number");
            String id = numberObj != null ? numberObj.toString() : null;

            String title = (String) item.get("title");
            String description = (String) item.get("body");
            String state = (String) item.get("state");
            String closedAtStr = (String) item.get("closed_at");
            String createdAtStr = (String) item.get("created_at");

            // Extract labels (Search API returns array of {name, color, ...} objects)
            List<Map<String, Object>> labelItems = (List<Map<String, Object>>) item.get("labels");
            List<String> labels = new ArrayList<>();
            if (labelItems != null) {
                for (Map<String, Object> label : labelItems) {
                    labels.add((String) label.get("name"));
                }
            }

            // Extract repository from repository_url
            String repoUrl = (String) item.get("repository_url");
            String repository = null;
            if (repoUrl != null) {
                // repository_url format: https://api.github.com/repos/owner/repo
                String[] urlParts = repoUrl.split("/repos/");
                if (urlParts.length > 1) {
                    repository = urlParts[1];
                }
            }
            if (repository == null) {
                repository = owner + "/" + repo;
            }

            // Parse dates
            LocalDateTime closedAt = null;
            if (closedAtStr != null && !closedAtStr.isEmpty()) {
                closedAt = LocalDateTime.parse(closedAtStr.replace("Z", ""),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } else if (createdAtStr != null && !createdAtStr.isEmpty()) {
                closedAt = LocalDateTime.parse(createdAtStr.replace("Z", ""),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }

            if (id == null || title == null || closedAt == null) {
                return null;
            }

            return new Issue(id, title, description, labels, closedAt,
                    properties.getAssignee(), null, repository);

        } catch (Exception e) {
            log.warn("Failed to map search issue: {}", e.getMessage());
            return null;
        }
    }

    private int parseProjectNumber() {
        String project = properties.getProject();
        if (project != null && !project.isBlank()) {
            try {
                return Integer.parseInt(project.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid project number: {}", project);
            }
        }
        return 7; // default
    }
}
