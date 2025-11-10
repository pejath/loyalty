package com.clubloyalty.server.dto;

import java.time.Instant;

public class NotificationDtos {
    public static class NotificationDto {
        public Long id;
        public String title;
        public String message;
        public String type;
        public Instant createdAt;
        public boolean read;
    }
}
