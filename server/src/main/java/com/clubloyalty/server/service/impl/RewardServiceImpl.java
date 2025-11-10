package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.Reward;
import com.clubloyalty.server.dto.RewardDtos.RewardDto;
import com.clubloyalty.server.dto.RewardDtos.RewardUpsertRequest;
import com.clubloyalty.server.repo.RewardRepository;
import com.clubloyalty.server.service.reward.RewardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RewardServiceImpl implements RewardService {
    private final RewardRepository rewards;

    public RewardServiceImpl(RewardRepository rewards) {
        this.rewards = rewards;
    }

    @Transactional(readOnly = true)
    public List<RewardDto> listActive() {
        return rewards.findByActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RewardDto> listAll() {
        return rewards.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public RewardDto create(RewardUpsertRequest request) {
        var reward = new Reward();
        reward.setTitle(request.title);
        reward.setDescription(request.description);
        reward.setCost(request.cost);
        reward.setActive(request.active == null || request.active);
        rewards.save(reward);
        return toDto(reward);
    }

    public RewardDto update(Long id, RewardUpsertRequest request) {
        var reward = rewards.findById(id).orElseThrow(() -> new RuntimeException("Reward not found"));
        reward.setTitle(request.title);
        reward.setDescription(request.description);
        reward.setCost(request.cost);
        if (request.active != null) {
            reward.setActive(request.active);
        }
        return toDto(reward);
    }

    public RewardDto setActive(Long id, boolean active) {
        var reward = rewards.findById(id).orElseThrow(() -> new RuntimeException("Reward not found"));
        reward.setActive(active);
        return toDto(reward);
    }

    public void delete(Long id) {
        if (!rewards.existsById(id)) {
            throw new RuntimeException("Reward not found");
        }
        rewards.deleteById(id);
    }

    private RewardDto toDto(Reward reward) {
        var dto = new RewardDto();
        dto.id = reward.getId();
        dto.title = reward.getTitle();
        dto.description = reward.getDescription();
        dto.cost = reward.getCost();
        dto.active = reward.isActive();
        return dto;
    }
}
