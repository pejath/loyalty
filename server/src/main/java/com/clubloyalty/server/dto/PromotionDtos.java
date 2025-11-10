package com.clubloyalty.server.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class PromotionDtos {
    public static class PromotionDto {
        public Long id;
        public String title;
        public String description;
        public Instant startAt;
        public Instant endAt;
        public String actionType;
        public Integer pointsAmount;
        public String notificationTitle;
        public String notificationMessage;
        public boolean executed;
    }

    public static class PromotionCreateRequest {
        @NotBlank
        public String title;
        @NotBlank
        public String description;
        @NotNull
        public Instant startAt;
        @NotNull
        public Instant endAt;
        @NotBlank
        public String actionType;
        public Integer pointsAmount;
        public String notificationTitle;
        public String notificationMessage;
    }
}

