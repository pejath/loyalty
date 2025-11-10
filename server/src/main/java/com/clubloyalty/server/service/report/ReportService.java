package com.clubloyalty.server.service.report;

import com.clubloyalty.server.dto.ReportDtos.SummaryResponse;

import java.time.Instant;

public interface ReportService {
    SummaryResponse summarize(Instant from, Instant to);
}
