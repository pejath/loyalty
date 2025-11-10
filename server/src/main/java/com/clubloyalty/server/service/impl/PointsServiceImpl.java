package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.*;
import com.clubloyalty.server.dto.PointsDtos.TransactionDto;
import com.clubloyalty.server.dto.RewardDtos.TxDto;
import com.clubloyalty.server.repo.BalanceRepository;
import com.clubloyalty.server.repo.MemberRepository;
import com.clubloyalty.server.repo.PointsTransactionRepository;
import com.clubloyalty.server.repo.RewardRepository;
import com.clubloyalty.server.service.notification.NotificationService;
import com.clubloyalty.server.service.points.PointsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PointsServiceImpl implements PointsService {
    private final MemberRepository members;
    private final BalanceRepository balances;
    private final RewardRepository rewards;
    private final PointsTransactionRepository txRepo;
    private final NotificationService notifications;

    public PointsServiceImpl(MemberRepository members,
                             BalanceRepository balances,
                             RewardRepository rewards,
                             PointsTransactionRepository txRepo,
                             NotificationService notifications) {
        this.members = members;
        this.balances = balances;
        this.rewards = rewards;
        this.txRepo = txRepo;
        this.notifications = notifications;
    }

    public TxDto earnByMinutes(Long memberId, long minutes) {
        return record(memberId, minutes, TxnType.EARN, null);
    }

    public TxDto earnByAmount(Long memberId, double amount) {
        return record(memberId, Math.round(amount), TxnType.EARN, null);
    }

    public TxDto adjust(Long memberId, long delta) {
        return record(memberId, delta, TxnType.ADJUST, null);
    }

    public TxDto redeem(Long memberId, Long rewardId) {
        var member = members.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        var balance = ensureBalance(member);
        var reward = rewards.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("Reward not found"));
        if (!reward.isActive()) throw new RuntimeException("Reward inactive");
        if (balance.getPoints() < reward.getCost()) throw new RuntimeException("Not enough points");

        balance.setPoints(balance.getPoints() - reward.getCost());
        balances.save(balance);
        String initiatedBy = resolveInitiator(member);
        var tx = txRepo.save(new PointsTransaction(member, -reward.getCost(), TxnType.REDEEM, reward, initiatedBy));
        return new TxDto(tx.getId(), -reward.getCost(), balance.getPoints());
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> history(Long memberId, String type, java.time.Instant from, java.time.Instant to, int page, int size) {
        TxnType txnType = null;
        if (type != null && !type.isBlank()) {
            try {
                txnType = TxnType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Unknown transaction type");
            }
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new RuntimeException("Invalid period");
        }
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PointsTransaction> txPage = txRepo.search(memberId, txnType, from, to, pageable);
        return txPage.map(this::toDto);
    }

    private TxDto record(Long memberId, long delta, TxnType type, Reward reward) {
        var member = members.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        var balance = ensureBalance(member);
        long updated = balance.getPoints() + delta;
        if (updated < 0) {
            throw new RuntimeException("Balance cannot go negative");
        }
        balance.setPoints(updated);
        balances.save(balance);
        String initiatedBy = resolveInitiator(member);
        var tx = txRepo.save(new PointsTransaction(member, delta, type, reward, initiatedBy));
    if (delta > 0 && notifications != null) {
      String title = "Points awarded";
      String message = "You received " + delta + " bonus points. Balance: " + balance.getPoints();
            notifications.createForMember(member.getId(), title, message, "POINTS", true);
        }
        return new TxDto(tx.getId(), delta, balance.getPoints());
    }

    private String resolveInitiator(Member member) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }
            if (auth.getName() != null) {
                return auth.getName();
            }
        }
        if (member.getUser() != null) {
            return member.getUser().getUsername();
        }
        return "system";
    }

    private Balance ensureBalance(Member member) {
        var balance = member.getBalance();
        if (balance == null) {
            balance = new Balance(member, 0);
            member.setBalance(balance);
            balances.save(balance);
        }
        return balance;
    }

    private TransactionDto toDto(PointsTransaction tx) {
        var dto = new TransactionDto();
        dto.id = tx.getId();
        dto.memberId = tx.getMember().getId();
        dto.memberName = tx.getMember().getFullName();
        dto.amount = tx.getAmount();
        dto.type = tx.getType().name();
        dto.rewardTitle = tx.getReward() != null ? tx.getReward().getTitle() : null;
        dto.createdAt = tx.getCreatedAt();
        return dto;
    }
}



