package com.clubloyalty.server.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class RewardDtos {
    public static class RewardDto {
        public Long id;
        public String title;
        public String description;
        public int cost;
        public boolean active;
    }

    public static class TxDto {
        public Long txId;
        public long delta;
        public long balance;

        public TxDto(Long t, long d, long b) {
            txId = t;
            delta = d;
            balance = b;
        }
    }

    public static class RewardUpsertRequest {
        @NotBlank
        public String title;
        @NotBlank
        public String description;
        @Min(0)
        public int cost;
        public Boolean active;
    }

    public static class RewardStatusRequest {
        @javax.validation.constraints.NotNull
        public Boolean active;
    }
}
