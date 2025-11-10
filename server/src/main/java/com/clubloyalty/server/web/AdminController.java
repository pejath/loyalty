package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.RewardDtos.TxDto;
import com.clubloyalty.server.dto.RuleDtos.RuleApplyDto;
import com.clubloyalty.server.service.points.PointsService;
import com.clubloyalty.server.service.rules.RuleEngine;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final RuleEngine re;
    private final PointsService ps;

    public AdminController(RuleEngine r, PointsService p) {
        re = r;
        ps = p;
    }

    @PostMapping("/rules/apply")
    @PreAuthorize("hasRole('ADMIN')")
    public TxDto apply(@RequestBody RuleApplyDto dto) {
        long pts = re.calculatePoints(dto.eventType, dto.context == null ? java.util.Collections.emptyMap() : dto.context);
        return ps.adjust(dto.memberId, pts);
    }
}
