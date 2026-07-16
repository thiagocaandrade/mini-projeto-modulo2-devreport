package com.devreport.controller;

import com.devreport.agent.DevReportAgent;
import com.devreport.config.GitHubProperties;
import com.devreport.controller.dto.*;
import com.devreport.dashboard.PdfService;
import com.devreport.domain.AnalysisRequest;
import com.devreport.domain.DashboardReport;
import com.devreport.infrastructure.github.GitHubClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final DevReportAgent agent;
    private final GitHubClient gitHubClient;
    private final GitHubProperties gitHubProperties;
    private final PdfService pdfService;

    public DashboardController(DevReportAgent agent, GitHubClient gitHubClient,
                                GitHubProperties gitHubProperties,
                                PdfService pdfService) {
        this.agent = agent;
        this.gitHubClient = gitHubClient;
        this.gitHubProperties = gitHubProperties;
        this.pdfService = pdfService;
    }

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("dashboard");
        String defaultRepo = gitHubProperties.getOwner() + "/" + gitHubProperties.getRepository();
        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setRepositories(List.of(defaultRepo));
        mv.addObject("request", request);
        mv.addObject("defaultRepo", defaultRepo);
        try {
            List<String> repos = fetchRepositoryNames();
            mv.addObject("availableRepos", repos);
        } catch (Exception e) {
            mv.addObject("availableRepos", Collections.emptyList());
        }
        return mv;
    }

    @GetMapping("/api/repositories")
    @ResponseBody
    public List<String> getRepositories() {
        return fetchRepositoryNames();
    }

    private List<String> fetchRepositoryNames() {
        try {
            String json = gitHubClient.fetchRepositories();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            List<String> repos = new ArrayList<>();
            if (root.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : root) {
                    if (node.has("full_name")) {
                        repos.add(node.get("full_name").asText());
                    }
                }
            }
            return repos;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @PostMapping("/")
    public ModelAndView analyze(@Valid AnalysisRequestDTO request, BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView("dashboard");
        mv.addObject("request", request);

        if (bindingResult.hasErrors()) {
            return mv;
        }

        // Fallback to default repo if none selected
        List<String> repos = request.getRepositories();
        if (repos == null || repos.isEmpty()) {
            String defaultRepo = gitHubProperties.getOwner() + "/" + gitHubProperties.getRepository();
            repos = List.of(defaultRepo);
        }
        AnalysisRequest domainRequest = new AnalysisRequest(
                request.getStartDate(), request.getEndDate(), repos);
        DashboardReport report = agent.analyze(domainRequest);
        DashboardDTO dashboard = toDto(report);

        mv.addObject("dashboard", dashboard);
        return mv;
    }

    private DashboardDTO toDto(DashboardReport report) {
        DashboardDTO dto = new DashboardDTO();

        if (report.getMetrics() != null) {
            MetricDTO metrics = new MetricDTO();
            metrics.setTotal(report.getMetrics().getTotal());
            metrics.setFeatures(report.getMetrics().getFeatures());
            metrics.setBugs(report.getMetrics().getBugs());
            metrics.setTasks(report.getMetrics().getTasks());
            metrics.setThroughputPerWeek(report.getMetrics().getThroughputPerWeek());
            metrics.setAvgResolutionDays(report.getMetrics().getAvgResolutionDays());
            metrics.setBugDensityPercent(report.getMetrics().getBugDensityPercent());
            dto.setMetrics(metrics);
        }

        if (report.getPeriodChart() != null) {
            dto.setPeriodLabels(report.getPeriodChart().getLabels());
            dto.setPeriodValues(report.getPeriodChart().getValues());
        }

        if (report.getCategoryChart() != null) {
            dto.setCategoryLabels(report.getCategoryChart().getLabels());
            dto.setCategoryValues(report.getCategoryChart().getValues());
        }

        if (report.getSummary() != null) {
            dto.setSummary(report.getSummary().getContent());
        }

        dto.setMessage(report.getMessage());

        // PR Metrics
        if (report.getPrMetrics() != null) {
            PRMetricsDTO prMetrics = new PRMetricsDTO();
            prMetrics.setTotalMerged(report.getPrMetrics().getTotalMerged());
            prMetrics.setTotalAdditions(report.getPrMetrics().getTotalAdditions());
            prMetrics.setTotalDeletions(report.getPrMetrics().getTotalDeletions());
            prMetrics.setTotalChangedFiles(report.getPrMetrics().getTotalChangedFiles());
            prMetrics.setAverageTimeToMerge(report.getPrMetrics().getAverageTimeToMerge());
            prMetrics.setUniqueReviewers(report.getPrMetrics().getUniqueReviewers());
            prMetrics.setPrSizeDistribution(report.getPrMetrics().getPrSizeDistribution());
            prMetrics.setReviewCoveragePercent(report.getPrMetrics().getReviewCoveragePercent());
            dto.setPrMetrics(prMetrics);
        }

        // PR Size Chart
        if (report.getPrSizeChart() != null) {
            dto.setPrSizeLabels(report.getPrSizeChart().getLabels());
            dto.setPrSizeValues(report.getPrSizeChart().getValues());
        }

        // Repository Summaries
        if (report.getRepositorySummaries() != null) {
            dto.setRepositorySummaries(report.getRepositorySummaries().stream()
                    .map(rs -> {
                        RepositorySummaryDTO rsDto = new RepositorySummaryDTO();
                        rsDto.setName(rs.getName());
                        rsDto.setTotalIssues(rs.getTotalIssues());
                        rsDto.setTotalPRs(rs.getTotalPRs());
                        rsDto.setTotalAdditions(rs.getTotalAdditions());
                        return rsDto;
                    })
                    .collect(Collectors.toList()));
        }

        dto.setRepositoriesCount(report.getRepositoriesCount());

        return dto;
    }

    @GetMapping("/dashboard/export")
    public ResponseEntity<byte[]> exportPdf(@RequestParam LocalDate start,
                                             @RequestParam LocalDate end,
                                             @RequestParam(required = false) List<String> repositories) {
        AnalysisRequest request = new AnalysisRequest(start, end, repositories);
        DashboardReport report = agent.analyze(request);

        // Convert HTML to real PDF binary using openhtmltopdf (TASK-055)
        byte[] pdfBytes = pdfService.generatePdf(report, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment()
                .filename("devreport-" + start + "-to-" + end + ".pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
