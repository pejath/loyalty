package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<Notification> findByMemberIdAndReadAtIsNull(Long memberId);

    List<Notification> findByCreatedAtBeforeAndReadAtIsNull(Instant threshold);
}
