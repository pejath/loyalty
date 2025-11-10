package com.clubloyalty.server.repo;

import com.clubloyalty.server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByPhone(String phone);

    Optional<Member> findByUserUsername(String username);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<Member> findByUserId(Long userId);
}
