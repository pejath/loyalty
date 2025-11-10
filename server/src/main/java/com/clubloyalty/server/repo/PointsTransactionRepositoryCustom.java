package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.PointsTransaction;
import com.clubloyalty.server.domain.TxnType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface PointsTransactionRepositoryCustom {
    Page<PointsTransaction> search(Long memberId, TxnType type, Instant from, Instant to, Pageable pageable);
}
