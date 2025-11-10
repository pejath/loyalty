package com.clubloyalty.server.dto;

import java.time.Instant;

public class PointsDtos {
    public static class TransactionDto {
        public Long id;
        public Long memberId;
        public String memberName;
        public long amount;
        public String type;
        public String rewardTitle;
        public Instant createdAt;
    }
}
