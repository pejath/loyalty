package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.TxnType;
import com.clubloyalty.server.dto.ReportDtos.RewardStat;
import com.clubloyalty.server.dto.ReportDtos.SummaryResponse;
import com.clubloyalty.server.repo.MemberRepository;
import com.clubloyalty.server.service.report.ReportService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final MemberRepository members;

    @PersistenceContext
    private EntityManager em;

    public ReportServiceImpl(MemberRepository members) {
        this.members = members;
    }

    @Override
    public SummaryResponse summarize(Instant from, Instant to) {
        SummaryResponse dto = new SummaryResponse();
        dto.from = from;
        dto.to = to;
        dto.totalMembers = members.count();

        Map<String, Long> byType = aggregateTransactionsByType(from, to);
        dto.transactionsByType = byType;
        dto.totalTransactions = byType.values().stream().mapToLong(Long::longValue).sum();
        dto.totalPointsDelta = aggregateTotalDelta(from, to);
        dto.activeMembers = countActiveMembers(from, to);
        dto.avgTransactionsPerActiveMember =
                dto.activeMembers == 0 ? 0 : (double) dto.totalTransactions / dto.activeMembers;
        dto.topRewards = topRewards(from, to);
        dto.pointsSpentPerDay = aggregatePointsSpentPerDay(from, to);
        return dto;
    }

    private Map<String, Long> aggregateTransactionsByType(Instant from, Instant to) {
        StringBuilder jpql =
                new StringBuilder(
                        "select t.type, count(t) from PointsTransaction t where 1=1");
        if (from != null) {
            jpql.append(" and t.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and t.createdAt <= :to");
        }
        jpql.append(" group by t.type");

        TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }

        Map<String, Long> byType = new LinkedHashMap<>();
        for (Object[] row : query.getResultList()) {
            TxnType type = (TxnType) row[0];
            Long count = (Long) row[1];
            byType.put(type.name(), count == null ? 0L : count);
        }
        return byType;
    }

    private long aggregateTotalDelta(Instant from, Instant to) {
        StringBuilder jpql =
                new StringBuilder("select coalesce(sum(t.amount), 0) from PointsTransaction t where 1=1");
        if (from != null) {
            jpql.append(" and t.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and t.createdAt <= :to");
        }
        var query = em.createQuery(jpql.toString(), Long.class);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        Long result = query.getSingleResult();
        return result == null ? 0L : result;
    }

    private long countActiveMembers(Instant from, Instant to) {
        StringBuilder jpql =
                new StringBuilder(
                        "select count(distinct t.member.id) from PointsTransaction t where 1=1");
        if (from != null) {
            jpql.append(" and t.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and t.createdAt <= :to");
        }
        var query = em.createQuery(jpql.toString(), Long.class);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        Long result = query.getSingleResult();
        return result == null ? 0L : result;
    }

    private List<RewardStat> topRewards(Instant from, Instant to) {
        StringBuilder jpql =
                new StringBuilder(
                        "select new com.clubloyalty.server.dto.ReportDtos$RewardStat(r.id, r.title, count(t)) "
                                + "from PointsTransaction t join t.reward r where 1=1");
        if (from != null) {
            jpql.append(" and t.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and t.createdAt <= :to");
        }
        jpql.append(" group by r.id, r.title order by count(t) desc");

        TypedQuery<RewardStat> query = em.createQuery(jpql.toString(), RewardStat.class);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        query.setMaxResults(5);
        return query.getResultList();
    }

    private Map<String, Long> aggregatePointsSpentPerDay(Instant from, Instant to) {
        StringBuilder jpql = new StringBuilder(
                "select t.createdAt, t.amount from PointsTransaction t where t.type = com.clubloyalty.server.domain.TxnType.REDEEM");
        if (from != null) {
            jpql.append(" and t.createdAt >= :from");
        }
        if (to != null) {
            jpql.append(" and t.createdAt <= :to");
        }
        TypedQuery<Object[]> query = em.createQuery(jpql.toString(), Object[].class);
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        Map<String, Long> byDay = new LinkedHashMap<>();
        for (Object[] row : query.getResultList()) {
            Instant created = (Instant) row[0];
            long amount = (Long) row[1];
            LocalDate day = created.atZone(ZoneId.systemDefault()).toLocalDate();
            String key = day.toString();
            long spent = Math.abs(amount);
            byDay.put(key, byDay.getOrDefault(key, 0L) + spent);
        }
        return byDay;
    }
}
