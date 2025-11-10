package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.RewardDtos.RewardDto;
import com.clubloyalty.server.dto.RewardDtos.TxDto;
import com.clubloyalty.server.service.points.PointsService;
import com.clubloyalty.server.service.reward.RewardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rs;
    private final PointsService ps;

    public RewardController(RewardService r, PointsService p) {
        rs = r;
        ps = p;
    }

    @GetMapping
    public java.util.List<RewardDto> list() {
        return rs.listActive();
    }

    @PostMapping("/redeem/{rewardId}/member/{memberId}")
    public TxDto redeem(@PathVariable Long rewardId, @PathVariable Long memberId) {
        return ps.redeem(memberId, rewardId);
    }
}
