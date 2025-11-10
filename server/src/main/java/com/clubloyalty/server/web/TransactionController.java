package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.PageResponse;
import com.clubloyalty.server.dto.PointsDtos.TransactionDto;
import com.clubloyalty.server.repo.MemberRepository;
import com.clubloyalty.server.service.points.PointsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final PointsService points;
    private final MemberRepository members;

    public TransactionController(PointsService points, MemberRepository members) {
        this.points = points;
        this.members = members;
    }

    @GetMapping
    public PageResponse<TransactionDto> myTransactions(Authentication auth,
                                                       @RequestParam(value = "type", required = false) String type,
                                                       @RequestParam(value = "from", required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.Instant from,
                                                       @RequestParam(value = "to", required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.Instant to,
                                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                                       @RequestParam(value = "size", defaultValue = "20") int size) {
        var member = members.findByUserUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        var data = points.history(member.getId(), type, from, to, Math.max(page, 0), Math.max(size, 1));
        return PageResponse.from(data);
    }
}
