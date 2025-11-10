package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.Member;
import com.clubloyalty.server.domain.Promotion;
import com.clubloyalty.server.domain.Promotion.ActionType;
import com.clubloyalty.server.dto.PromotionDtos.PromotionCreateRequest;
import com.clubloyalty.server.dto.PromotionDtos.PromotionDto;
import com.clubloyalty.server.repo.MemberRepository;
import com.clubloyalty.server.repo.PromotionRepository;
import com.clubloyalty.server.service.notification.NotificationService;
import com.clubloyalty.server.service.points.PointsService;
import com.clubloyalty.server.service.promotion.PromotionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotions;
    private final MemberRepository members;
    private final PointsService points;
    private final NotificationService notifications;

    public PromotionServiceImpl(PromotionRepository promotions,
                                MemberRepository members,
                                PointsService points,
                                NotificationService notifications) {
        this.promotions = promotions;
        this.members = members;
        this.points = points;
        this.notifications = notifications;
    }

    public PromotionDto create(PromotionCreateRequest request) {
        validateRequest(request);
        Promotion promotion = new Promotion();
        promotion.setTitle(request.title);
        promotion.setDescription(request.description);
        promotion.setStartAt(request.startAt);
        promotion.setEndAt(request.endAt);
        promotion.setActionType(ActionType.valueOf(request.actionType.toUpperCase()));
        promotion.setPointsAmount(request.pointsAmount);
        promotion.setNotificationTitle(request.notificationTitle);
        promotion.setNotificationMessage(request.notificationMessage);
        promotions.save(promotion);
        return toDto(promotion);
    }

    @Transactional(readOnly = true)
    public List<PromotionDto> list() {
        return promotions.findAllByOrderByStartAtAsc().stream().map(this::toDto).collect(Collectors.toList());
    }

    public void executeDuePromotions() {
        java.time.Instant now = java.time.Instant.now();
        var due = promotions.findByExecutedFalseAndStartAtLessThanEqualAndEndAtGreaterThanEqual(now, now);
        for (Promotion promotion : due) {
            executePromotion(promotion);
        }
    }

    private void executePromotion(Promotion promotion) {
        if (promotion.isExecuted()) return;
        if (promotion.getActionType() == ActionType.AWARD_POINTS) {
            int amount = promotion.getPointsAmount() == null ? 0 : promotion.getPointsAmount();
            if (amount <= 0) throw new RuntimeException("Promotion points amount must be positive");
            List<Member> allMembers = members.findAll();
            for (Member member : allMembers) {
                points.adjust(member.getId(), amount);
                notifications.createForMember(member.getId(), promotion.getTitle(),
                        "You received " + amount + " bonus points", "PROMOTION", true);
            }
        } else if (promotion.getActionType() == ActionType.SEND_NOTIFICATION) {
            String title = promotion.getNotificationTitle();
            String message = promotion.getNotificationMessage();
            if (title == null || title.isBlank() || message == null || message.isBlank()) {
                throw new RuntimeException("Promotion notification content missing");
            }
            notifications.createForAll(title, message, "PROMOTION", true);
        }
        promotion.setExecuted(true);
        promotions.save(promotion);
    }

    private void validateRequest(PromotionCreateRequest request) {
        if (request.title == null || request.title.isBlank()) throw new RuntimeException("Title required");
        if (request.description == null || request.description.isBlank())
            throw new RuntimeException("Description required");
        if (request.startAt == null || request.endAt == null) throw new RuntimeException("Start and end required");
        if (request.startAt.isAfter(request.endAt)) throw new RuntimeException("Start must be before end");
        if (request.actionType == null || request.actionType.isBlank())
            throw new RuntimeException("Action type required");
        ActionType actionType;
        try {
            actionType = ActionType.valueOf(request.actionType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Unknown action type");
        }
        if (actionType == ActionType.AWARD_POINTS) {
            if (request.pointsAmount == null || request.pointsAmount <= 0) {
                throw new RuntimeException("pointsAmount must be positive");
            }
        } else if (actionType == ActionType.SEND_NOTIFICATION) {
            if (request.notificationTitle == null || request.notificationTitle.isBlank()
                    || request.notificationMessage == null || request.notificationMessage.isBlank()) {
                throw new RuntimeException("Notification content required");
            }
        }
        if (request.startAt.isBefore(Instant.now().minusSeconds(60))) {
            throw new RuntimeException("Start time must be in the future");
        }
    }

    private PromotionDto toDto(Promotion promotion) {
        PromotionDto dto = new PromotionDto();
        dto.id = promotion.getId();
        dto.title = promotion.getTitle();
        dto.description = promotion.getDescription();
        dto.startAt = promotion.getStartAt();
        dto.endAt = promotion.getEndAt();
        dto.actionType = promotion.getActionType().name();
        dto.pointsAmount = promotion.getPointsAmount();
        dto.notificationTitle = promotion.getNotificationTitle();
        dto.notificationMessage = promotion.getNotificationMessage();
        dto.executed = promotion.isExecuted();
        return dto;
    }
}

