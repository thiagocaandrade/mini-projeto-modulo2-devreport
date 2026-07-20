package com.devreport.infrastructure.github;

import com.devreport.config.GitHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class GitHubIssueService {

    private static final Logger log = LoggerFactory.getLogger(GitHubIssueService.class);
    private static final int MAX_PAGES = 5;
    private static final int PER_PAGE = 100;

    private final GitHubClient gitHubClient;
    private final GitHubProperties properties;
    private final boolean mockMode;

    public GitHubIssueService(GitHubClient gitHubClient, GitHubProperties properties,
                              @Value("${devreport.mock-mode:false}") boolean mockMode) {
        this.gitHubClient = gitHubClient;
        this.properties = properties;
        this.mockMode = mockMode;
    }

    public String fetchClosedIssuesRaw() {
        if (mockMode) {
            return loadMockIssues();
        }
        return fetchAllIssuesRaw(properties.getOwner(), properties.getRepository());
    }

    public String fetchClosedIssuesRaw(String owner, String repo) {
        if (mockMode) {
            return loadMockIssues();
        }
        return fetchAllIssuesRaw(owner, repo);
    }

    private String loadMockIssues() {
        try {
            ClassPathResource resource = new ClassPathResource("mock-issues.json");
            byte[] bytes = resource.getInputStream().readAllBytes();
            // Strip UTF-8 BOM if present
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                bytes = java.util.Arrays.copyOfRange(bytes, 3, bytes.length);
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            log.info("Loaded {} bytes from mock-issues.json", bytes.length);
            return json;
        } catch (Exception e) {
            log.error("Failed to load mock-issues.json: {}", e.getMessage());
            return "[]";
        }
    }

    private String fetchAllIssuesRaw(String owner, String repo) {
        log.info("Fetching all issues (open+closed) from repo: {}/{}", owner, repo);
        StringBuilder allIssues = new StringBuilder();
        allIssues.append("[");

        int page = 1;
        boolean first = true;

        while (page <= MAX_PAGES) {
            log.debug("Fetching page {} of all issues for {}/{}", page, owner, repo);
            String response;
            try {
                response = gitHubClient.fetchIssuesForRepo(owner, repo, "closed", page, PER_PAGE);
            } catch (RuntimeException e) {
                log.error("GitHub fetch failed at page {} for {}/{}: {}", page, owner, repo, e.getMessage());
                throw new RuntimeException("Não foi possível consultar os dados do GitHub no momento.", e);
            }

            if (response == null || response.equals("[]")) {
                break;
            }

            if (!first) {
                allIssues.append(",");
            }
            // Inject repository field INSIDE each JSON object: replace leading { with {"repository":"owner/repo",
            // Remove both leading [ and trailing ] from the page response
            String repoField = "\"repository\":\"" + owner + "/" + repo + "\",";
            String injected = response.substring(1, response.length() - 1); // remove [ and ]
            injected = injected.replace("{\"", "{" + repoField + "\"");
            allIssues.append(injected);
            first = false;

            page++;
        }

        allIssues.append("]");
        log.info("Retrieved {} pages of all issues for {}/{}", page - 1, owner, repo);
        return allIssues.toString();
    }

    public String fetchClosedIssuesRaw(List<String> repositories) {
        if (mockMode) {
            return loadMockIssues();
        }
        if (repositories == null || repositories.isEmpty()) {
            return fetchClosedIssuesRaw();
        }

        log.info("Fetching closed issues from {} repositories", repositories.size());
        StringBuilder allIssues = new StringBuilder();
        allIssues.append("[");
        boolean firstRepo = true;

        for (String repoFull : repositories) {
            String[] parts = repoFull.split("/", 2);
            String owner = parts[0];
            String repo = parts.length > 1 ? parts[1] : repoFull;

            try {
                String repoJson = fetchClosedIssuesRaw(owner, repo);
                if (repoJson != null && !repoJson.equals("[]")) {
                    if (!firstRepo) {
                        allIssues.append(",");
                    }
                    allIssues.append(repoJson, 1, repoJson.length() - 1);
                    firstRepo = false;
                }
            } catch (RuntimeException e) {
                log.error("Failed to fetch issues for repo {}: {}", repoFull, e.getMessage());
                // Continue with other repos
            }
        }

        allIssues.append("]");
        log.info("Completed multi-repo issue fetch");
        return allIssues.toString();
    }
}
