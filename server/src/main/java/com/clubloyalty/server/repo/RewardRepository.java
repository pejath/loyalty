package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByActiveTrue();
}
