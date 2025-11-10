package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.PointsTransaction;
import com.clubloyalty.server.domain.TxnType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PointsTransactionRepositoryImpl implements PointsTransactionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<PointsTransaction> search(
            Long memberId, TxnType type, Instant from, Instant to, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<PointsTransaction> query = cb.createQuery(PointsTransaction.class);
        Root<PointsTransaction> root = query.from(PointsTransaction.class);
        List<Predicate> predicates = buildPredicates(cb, root, memberId, type, from, to);
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        query.orderBy(cb.desc(root.get("createdAt")));

        var typedQuery = em.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<PointsTransaction> results = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PointsTransaction> countRoot = countQuery.from(PointsTransaction.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, memberId, type, from, to);
        if (!countPredicates.isEmpty()) {
            countQuery.where(countPredicates.toArray(new Predicate[0]));
        }
        long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<PointsTransaction> root,
            Long memberId,
            TxnType type,
            Instant from,
            Instant to) {
        List<Predicate> predicates = new ArrayList<>();
        if (memberId != null) {
            predicates.add(cb.equal(root.get("member").get("id"), memberId));
        }
        if (type != null) {
            predicates.add(cb.equal(root.get("type"), type));
        }
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return predicates;
    }
}
