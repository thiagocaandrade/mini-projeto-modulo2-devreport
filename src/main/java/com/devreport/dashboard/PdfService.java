package com.devreport.dashboard;

import com.devreport.domain.AnalysisRequest;
import com.devreport.domain.DashboardReport;
import com.devreport.domain.PRMetrics;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdf(DashboardReport report, AnalysisRequest request) {
        try {
            // Render Thymeleaf template to HTML string
            Context context = new Context(Locale.forLanguageTag("pt-BR"));
            context.setVariable("report", report);
            context.setVariable("request", request);

            // Format dates for display
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            context.setVariable("startDateFormatted", request.getStartDate().format(dtf));
            context.setVariable("endDateFormatted", request.getEndDate().format(dtf));

            // Prepare PR metrics for template
            if (report.getPrMetrics() != null) {
                PRMetrics pr = report.getPrMetrics();
                context.setVariable("prMetrics", pr);
                context.setVariable("totalChangedLines", pr.getTotalAdditions() + pr.getTotalDeletions());
            }

            // Prepare summary content - sanitize to avoid XML issues
            // HTML tags are intentionally preserved for structured summary rendering
            String summaryContent = null;
            if (report.getSummary() != null) {
                summaryContent = report.getSummary().getContent();
                if (summaryContent != null) {
                    // Remove control characters that break XML parsing (keep tab, newline, carriage return)
                    summaryContent = summaryContent.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
                }
            }
            context.setVariable("summaryContent", summaryContent);

            String html = templateEngine.process("pdf-report", context);

            // Convert HTML to PDF using openhtmltopdf
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(baos);
            builder.run();

            log.debug("PDF generated successfully, size: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }
}
