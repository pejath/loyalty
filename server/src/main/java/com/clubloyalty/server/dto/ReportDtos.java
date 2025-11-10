package com.clubloyalty.server.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ReportDtos {

    public static class RewardStat {
        public Long rewardId;
        public String title;
        public long redemptions;

        public RewardStat(Long rewardId, String title, long redemptions) {
            this.rewardId = rewardId;
            this.title = title;
            this.redemptions = redemptions;
        }
    }

    public static class SummaryResponse {
        public Instant from;
        public Instant to;
        public long totalMembers;
        public long activeMembers;
        public long totalTransactions;
        public long totalPointsDelta;
        public double avgTransactionsPerActiveMember;
        public Map<String, Long> transactionsByType;
        public List<RewardStat> topRewards;
    }
}
