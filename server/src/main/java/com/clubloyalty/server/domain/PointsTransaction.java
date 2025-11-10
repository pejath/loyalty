package com.clubloyalty.server.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "points_tx")
public class PointsTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Member member;
    private long amount;
    @Enumerated(EnumType.STRING)
    private TxnType type;
    @ManyToOne
    private Reward reward;
    private Instant createdAt = Instant.now();
    @Column(name = "initiated_by")
    private String initiatedBy;

    public PointsTransaction() {
    }

    public PointsTransaction(Member m, long a, TxnType t, Reward r, String initiatedBy) {
        member = m;
        amount = a;
        type = t;
        reward = r;
        this.initiatedBy = initiatedBy;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public long getAmount() {
        return amount;
    }

    public TxnType getType() {
        return type;
    }

    public Reward getReward() {
        return reward;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }
}
