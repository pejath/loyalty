package com.clubloyalty.server.service.points;

import com.clubloyalty.server.dto.PointsDtos.TransactionDto;
import com.clubloyalty.server.dto.RewardDtos.TxDto;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface PointsService {
    TxDto earnByMinutes(Long memberId, long minutes);

    TxDto earnByAmount(Long memberId, double amount);

    TxDto adjust(Long memberId, long delta);

    TxDto redeem(Long memberId, Long rewardId);

    Page<TransactionDto> history(Long memberId, String type, Instant from, Instant to, int page, int size);
}
