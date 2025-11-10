package com.clubloyalty.server.service.promotion;

import com.clubloyalty.server.dto.PromotionDtos.PromotionCreateRequest;
import com.clubloyalty.server.dto.PromotionDtos.PromotionDto;

import java.util.List;

public interface PromotionService {
    PromotionDto create(PromotionCreateRequest request);

    List<PromotionDto> list();

    void executeDuePromotions();
}

