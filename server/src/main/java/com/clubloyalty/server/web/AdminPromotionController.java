package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.PromotionDtos.PromotionCreateRequest;
import com.clubloyalty.server.dto.PromotionDtos.PromotionDto;
import com.clubloyalty.server.service.promotion.PromotionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promotions")
public class AdminPromotionController {
    private final PromotionService promotions;

    public AdminPromotionController(PromotionService promotions) {
        this.promotions = promotions;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<PromotionDto> list() {
        return promotions.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PromotionDto create(@RequestBody @Validated PromotionCreateRequest request) {
        return promotions.create(request);
    }
}
