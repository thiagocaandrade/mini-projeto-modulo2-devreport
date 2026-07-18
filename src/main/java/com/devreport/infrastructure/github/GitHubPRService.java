package com.devreport.infrastructure.github;

import com.devreport.config.GitHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubPRService {

    private static final Logger log = LoggerFactory.getLogger(GitHubPRService.class);
    private static final int MAX_PAGES = 5;
    private static final int PER_PAGE = 100;

    private final GitHubClient gitHubClient;
    private final GitHubProperties properties;
    private final boolean mockMode;

    public GitHubPRService(GitHubClient gitHubClient, GitHubProperties properties,
                           @Value("${devreport.mock-mode:false}") boolean mockMode) {
        this.gitHubClient = gitHubClient;
        this.properties = properties;
        this.mockMode = mockMode;
    }

    public String fetchMergedPRsRaw() {
        if (mockMode) return "[]";
        return fetchMergedPRsRaw(properties.getOwner(), properties.getRepository());
    }

    public String fetchMergedPRsRaw(String owner, String repo) {
        if (mockMode) return "[]";
        log.info("Fetching merged PRs from repo: {}/{}", owner, repo);
        StringBuilder allPRs = new StringBuilder();
        allPRs.append("[");

        int page = 1;
        boolean first = true;

        while (page <= MAX_PAGES) {
            log.debug("Fetching page {} of PRs for {}/{}", page, owner, repo);
            String response;
            try {
                response = gitHubClient.fetchPullRequests(owner, repo, page, PER_PAGE);
            } catch (RuntimeException e) {
                log.error("GitHub PR fetch failed at page {} for {}/{}: {}", page, owner, repo, e.getMessage());
                throw new RuntimeException("Não foi possível consultar os Pull Requests do GitHub no momento.", e);
            }

            if (response == null || response.equals("[]")) {
                break;
            }

            if (!first) {
                allPRs.append(",");
            }
            // Inject repository field INSIDE each JSON object: replace leading { with {"repository":"owner/repo",
            String repoField = "\"repository\":\"" + owner + "/" + repo + "\",";
            String injected = response.substring(1); // remove leading [
            injected = injected.replace("{\"", "{" + repoField + "\"");
            allPRs.append(injected);
            first = false;

            page++;
        }

        allPRs.append("]");
        log.info("Retrieved {} pages of PRs for {}/{}", page - 1, owner, repo);
        return allPRs.toString();
    }

    public String fetchMergedPRsRaw(List<String> repositories) {
        if (mockMode) return "[]";
        if (repositories == null || repositories.isEmpty()) {
            return fetchMergedPRsRaw();
        }

        log.info("Fetching merged PRs from {} repositories", repositories.size());
        StringBuilder allPRs = new StringBuilder();
        allPRs.append("[");
        boolean firstRepo = true;

        for (String repoFull : repositories) {
            String[] parts = repoFull.split("/", 2);
            String owner = parts[0];
            String repo = parts.length > 1 ? parts[1] : repoFull;

            try {
                String repoJson = fetchMergedPRsRaw(owner, repo);
                if (repoJson != null && !repoJson.equals("[]")) {
                    if (!firstRepo) {
                        allPRs.append(",");
                    }
                    allPRs.append(repoJson, 1, repoJson.length() - 1);
                    firstRepo = false;
                }
            } catch (RuntimeException e) {
                log.error("Failed to fetch PRs for repo {}: {}", repoFull, e.getMessage());
                // Continue with other repos
            }
        }

        allPRs.append("]");
        log.info("Completed multi-repo PR fetch");
        return allPRs.toString();
    }
}
