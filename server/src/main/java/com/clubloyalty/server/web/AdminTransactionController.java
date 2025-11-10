package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.PageResponse;
import com.clubloyalty.server.dto.PointsDtos.TransactionDto;
import com.clubloyalty.server.service.points.PointsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {
    private final PointsService points;

    public AdminTransactionController(PointsService points) {
        this.points = points;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public PageResponse<TransactionDto> list(
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.Instant to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        var data = points.history(memberId, type, from, to, Math.max(page, 0), Math.max(size, 1));
        return PageResponse.from(data);
    }
}
