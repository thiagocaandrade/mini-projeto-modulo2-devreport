package com.devreport.controller;

import com.devreport.agent.DevReportAgent;
import com.devreport.controller.dto.*;
import com.devreport.dashboard.PdfService;
import com.devreport.domain.AnalysisRequest;
import com.devreport.domain.DashboardReport;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Controller
public class DashboardController {

    private final DevReportAgent agent;
    private final PdfService pdfService;

    public DashboardController(DevReportAgent agent, PdfService pdfService) {
        this.agent = agent;
        this.pdfService = pdfService;
    }

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("dashboard");
        mv.addObject("request", new AnalysisRequestDTO());
        return mv;
    }

    @PostMapping("/")
    public ModelAndView analyze(@Valid AnalysisRequestDTO request, BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView("dashboard");
        mv.addObject("request", request);

        if (bindingResult.hasErrors()) {
            return mv;
        }

        AnalysisRequest domainRequest = new AnalysisRequest(
                request.getStartDate(), request.getEndDate(), Collections.emptyList());
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

        return dto;
    }

    @GetMapping("/dashboard/export")
    public ResponseEntity<byte[]> exportPdf(@RequestParam LocalDate start,
                                             @RequestParam LocalDate end) {
        AnalysisRequest request = new AnalysisRequest(start, end, Collections.emptyList());
        DashboardReport report = agent.analyze(request);

        byte[] pdfBytes = pdfService.generatePdf(report, request);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment()
                .filename("devreport-" + start.format(dtf) + "-a-" + end.format(dtf) + ".pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
