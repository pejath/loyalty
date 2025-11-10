package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long>, PointsTransactionRepositoryCustom {
}
