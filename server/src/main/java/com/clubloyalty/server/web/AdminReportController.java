package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.ReportDtos.SummaryResponse;
import com.clubloyalty.server.service.report.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reports;

    public AdminReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public SummaryResponse summary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return reports.summarize(parse(from), parse(to));
    }

    private Instant parse(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        return Instant.parse(iso);
    }
}
