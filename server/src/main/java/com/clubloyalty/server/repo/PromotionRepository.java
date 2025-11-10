package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByExecutedFalseAndStartAtLessThanEqualAndEndAtGreaterThanEqual(Instant start, Instant end);

    List<Promotion> findAllByOrderByStartAtAsc();
}
