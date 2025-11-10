package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.service.promotion.PromotionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PromotionScheduler {
    private final PromotionService promotionService;

    public PromotionScheduler(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @Scheduled(fixedDelay = 60000)
    public void processPromotions() {
        promotionService.executeDuePromotions();
    }
}
