package com.devreport.dashboard;

import com.devreport.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService();
    }

    @Test
    void shouldBuildDashboardWithMetricsAndCharts() {
        Metric metrics = new Metric(10, 5, 3, 2);
        ChartData periodChart = new ChartData(List.of("2026-07-01"), List.of(10));
        ChartData categoryChart = new ChartData(
                List.of("Features", "Bugs", "Tasks"),
                List.of(5, 3, 2)
        );

        DashboardReport report = dashboardService.build(metrics, periodChart, categoryChart, null, null);

        assertNotNull(report);
        assertEquals(10, report.getMetrics().getTotal());
        assertEquals(5, report.getMetrics().getFeatures());
        assertEquals(3, report.getMetrics().getBugs());
        assertEquals(2, report.getMetrics().getTasks());
        assertNotNull(report.getPeriodChart());
        assertNotNull(report.getCategoryChart());
        assertNull(report.getSummary());
        assertNull(report.getMessage());
    }

    @Test
    void shouldBuildDashboardWithSummary() {
        Metric metrics = new Metric(5, 3, 1, 1);
        ChartData periodChart = new ChartData(List.of("2026-07-01"), List.of(5));
        ChartData categoryChart = new ChartData(List.of("A", "B", "C"), List.of(3, 1, 1));
        Insight summary = new Insight("Excelente trabalho no período!");

        DashboardReport report = dashboardService.build(metrics, periodChart, categoryChart, summary, null);

        assertNotNull(report);
        assertNotNull(report.getSummary());
        assertEquals("Excelente trabalho no período!", report.getSummary().getContent());
    }

    @Test
    void shouldBuildDashboardWithMessage() {
        Metric metrics = new Metric(0, 0, 0, 0);
        ChartData periodChart = new ChartData(List.of(), List.of());
        ChartData categoryChart = new ChartData(List.of("Features", "Bugs", "Tasks"), List.of(0, 0, 0));

        DashboardReport report = dashboardService.build(
                metrics, periodChart, categoryChart, null,
                "Não existem entregas concluídas para o período informado."
        );

        assertNotNull(report);
        assertEquals("Não existem entregas concluídas para o período informado.", report.getMessage());
        assertNull(report.getSummary());
    }

    @Test
    void shouldBuildDashboardWithErrorState() {
        DashboardReport report = dashboardService.build(
                null, null, null, null,
                "Não foi possível consultar os dados do GitHub no momento."
        );

        assertNotNull(report);
        assertNull(report.getMetrics());
        assertEquals("Não foi possível consultar os dados do GitHub no momento.", report.getMessage());
    }

    @Test
    void shouldBuildDashboardWithNullCharts() {
        Metric metrics = new Metric(3, 1, 1, 1);

        DashboardReport report = dashboardService.build(metrics, null, null, null, null);

        assertNotNull(report);
        assertEquals(3, report.getMetrics().getTotal());
        assertNull(report.getPeriodChart());
        assertNull(report.getCategoryChart());
    }
}
