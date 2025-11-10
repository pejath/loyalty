package com.clubloyalty.server.service.reward;

import com.clubloyalty.server.dto.RewardDtos.RewardDto;
import com.clubloyalty.server.dto.RewardDtos.RewardUpsertRequest;

import java.util.List;

public interface RewardService {
    List<RewardDto> listActive();

    List<RewardDto> listAll();

    RewardDto create(RewardUpsertRequest request);

    RewardDto update(Long id, RewardUpsertRequest request);

    RewardDto setActive(Long id, boolean active);

    void delete(Long id);
}
