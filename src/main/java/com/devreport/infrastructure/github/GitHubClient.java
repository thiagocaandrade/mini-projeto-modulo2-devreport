package com.devreport.infrastructure.github;

import com.devreport.config.GitHubProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";

    private final RestClient restClient;
    private final GitHubProperties properties;

    public GitHubClient(GitHubProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(GITHUB_API_BASE)
                .defaultHeader("Authorization", "Bearer " + properties.getToken())
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
        log.info("GitHubClient initialized for owner={}, repository={}",
                properties.getOwner(), properties.getRepository());
    }

    public String fetchIssues(String state, int page, int perPage) {
        return fetchIssuesForRepo(properties.getOwner(), properties.getRepository(), state, page, perPage);
    }

    public String fetchIssuesForRepo(String owner, String repo, String state, int page, int perPage) {
        String assignee = properties.getAssignee();
        log.debug("Fetching repo issues: owner={}, repo={}, state={}, assignee={}, page={}, perPage={}",
                owner, repo, state, assignee, page, perPage);
        try {
            StringBuilder uri = new StringBuilder(
                    "/repos/{owner}/{repo}/issues?state={state}&page={page}&per_page={perPage}&filter=all");
            if (assignee != null && !assignee.isBlank()) {
                uri.append("&assignee={assignee}");
            }
            return restClient.get()
                    .uri(uri.toString(), owner, repo,
                            state, page, perPage, assignee)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            return handleClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("GitHub API server error: HTTP {}", e.getStatusCode().value());
            throw new RuntimeException("API do GitHub indisponível no momento. Tente novamente mais tarde.");
        } catch (RestClientException e) {
            log.error("GitHub connection failed: {}", e.getMessage());
            throw new RuntimeException("Não foi possível conectar ao GitHub. Verifique sua conexão de rede.");
        }
    }

    public String fetchOrgIssues(String state, int page, int perPage) {
        String org = properties.getOwner();
        String assignee = properties.getAssignee();
        log.debug("Fetching org issues: org={}, state={}, assignee={}, page={}, perPage={}",
                org, state, assignee, page, perPage);
        try {
            StringBuilder uri = new StringBuilder(
                    "/orgs/{org}/issues?state={state}&filter=all&page={page}&per_page={perPage}");
            if (assignee != null && !assignee.isBlank()) {
                uri.append("&assignee={assignee}");
            }
            return restClient.get()
                    .uri(uri.toString(), org, state, page, perPage, assignee)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            return handleClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("GitHub API server error: HTTP {}", e.getStatusCode().value());
            throw new RuntimeException("API do GitHub indisponível no momento. Tente novamente mais tarde.");
        } catch (RestClientException e) {
            log.error("GitHub connection failed: {}", e.getMessage());
            throw new RuntimeException("Não foi possível conectar ao GitHub. Verifique sua conexão de rede.");
        }
    }

    private String handleClientError(HttpClientErrorException e) {
        int statusCode = e.getStatusCode().value();
        switch (statusCode) {
            case 401, 403 -> {
                log.warn("GitHub authentication failed - check your token");
                throw new RuntimeException(
                        "Falha na autenticação com o GitHub. Verifique seu Personal Access Token.");
            }
            case 404 -> {
                log.warn("Repository not found: {}/{}", properties.getOwner(), properties.getRepository());
                throw new RuntimeException(
                        "Repositório não encontrado: " + properties.getOwner() + "/" + properties.getRepository()
                        + ". Verifique a configuração.");
            }
            default -> {
                log.error("GitHub API client error: HTTP {}", statusCode);
                throw new RuntimeException("Erro ao consultar o GitHub (HTTP " + statusCode + ").");
            }
        }
    }

    public GitHubProperties getProperties() {
        return properties;
    }

    public String fetchRepositories() {
        String org = properties.getOwner();
        log.debug("Fetching repositories for org: {}", org);
        try {
            // Try org endpoint first (for organization owners)
            return restClient.get()
                    .uri("/orgs/{org}/repos?type=all&sort=updated&per_page=100&direction=desc", org)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            // Fallback to user endpoint
            try {
                return restClient.get()
                        .uri("/users/{user}/repos?type=all&sort=updated&per_page=100&direction=desc", org)
                        .retrieve()
                        .body(String.class);
            } catch (HttpClientErrorException e2) {
                return handleClientError(e2);
            }
        } catch (HttpServerErrorException e) {
            log.error("GitHub API server error fetching repos: HTTP {}", e.getStatusCode().value());
            throw new RuntimeException("API do GitHub indisponível no momento.");
        } catch (RestClientException e) {
            log.error("GitHub connection failed fetching repos: {}", e.getMessage());
            throw new RuntimeException("Não foi possível conectar ao GitHub.");
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchGraphQL(String query, Map<String, Object> variables) {
        log.debug("Executing GraphQL query");
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> body = Map.of("query", query, "variables", variables);
            String jsonBody = mapper.writeValueAsString(body);

            String response = restClient.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            return mapper.readValue(response, Map.class);
        } catch (HttpClientErrorException e) {
            return handleClientErrorAsMap(e);
        } catch (HttpServerErrorException e) {
            log.error("GitHub GraphQL API server error: HTTP {}", e.getStatusCode().value());
            throw new RuntimeException("API do GitHub GraphQL indisponível no momento.");
        } catch (Exception e) {
            log.error("GitHub GraphQL request failed: {}", e.getMessage());
            throw new RuntimeException("Falha na consulta GraphQL ao GitHub: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleClientErrorAsMap(HttpClientErrorException e) {
        int statusCode = e.getStatusCode().value();
        log.error("GitHub GraphQL API client error: HTTP {}", statusCode);
        return Map.of("error", "HTTP " + statusCode + ": " + e.getMessage());
    }

    public String fetchPullRequests(String owner, String repo, int page, int perPage) {
        log.debug("Fetching PRs: owner={}, repo={}, page={}, perPage={}", owner, repo, page, perPage);
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}/pulls?state=closed&page={page}&per_page={perPage}&sort=updated&direction=desc",
                            owner, repo, page, perPage)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            return handleClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("GitHub API server error fetching PRs: HTTP {}", e.getStatusCode().value());
            throw new RuntimeException("API do GitHub indisponível no momento. Tente novamente mais tarde.");
        } catch (RestClientException e) {
            log.error("GitHub connection failed fetching PRs: {}", e.getMessage());
            throw new RuntimeException("Não foi possível conectar ao GitHub. Verifique sua conexão de rede.");
        }
    }

    /**
     * Search issues via GitHub REST Search API.
     * Supports qualifiers: org, assignee, repo, is, state, label, project, etc.
     * Example query: "org:myorg assignee:myuser is:issue state:open"
     */
    public String searchIssues(String query, int page, int perPage) {
        log.debug("Searching issues: query={}, page={}, perPage={}", query, page, perPage);
        try {
            return restClient.get()
                    .uri("/search/issues?q={query}&page={page}&per_page={perPage}",
                            query, page, perPage)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            return handleClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("GitHub Search API server error: HTTP {}", e.getStatusCode().value());
            throw new RuntimeException("API de busca do GitHub indisponível no momento.");
        } catch (RestClientException e) {
            log.error("GitHub Search connection failed: {}", e.getMessage());
            throw new RuntimeException("Não foi possível conectar ao GitHub. Verifique sua conexão de rede.");
        }
    }
}
