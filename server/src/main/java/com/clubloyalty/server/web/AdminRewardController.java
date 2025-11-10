package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.AuthDtos.SimpleResponse;
import com.clubloyalty.server.dto.RewardDtos.RewardDto;
import com.clubloyalty.server.dto.RewardDtos.RewardStatusRequest;
import com.clubloyalty.server.dto.RewardDtos.RewardUpsertRequest;
import com.clubloyalty.server.service.reward.RewardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rewards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardController {
    private final RewardService rewards;

    public AdminRewardController(RewardService rewards) {
        this.rewards = rewards;
    }

    @GetMapping
    public List<RewardDto> list() {
        return rewards.listAll();
    }

    @PostMapping
    public RewardDto create(@RequestBody @Validated RewardUpsertRequest request) {
        return rewards.create(request);
    }

    @PutMapping("/{id}")
    public RewardDto update(@PathVariable Long id, @RequestBody @Validated RewardUpsertRequest request) {
        return rewards.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public RewardDto setStatus(@PathVariable Long id, @RequestBody @Validated RewardStatusRequest request) {
        return rewards.setActive(id, request.active);
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable Long id) {
        rewards.delete(id);
        return new SimpleResponse("OK");
    }
}
