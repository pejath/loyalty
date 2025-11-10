package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
}
