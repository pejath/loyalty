package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TierRepository extends JpaRepository<Tier, Long> {
    Optional<Tier> findByName(String name);
}
