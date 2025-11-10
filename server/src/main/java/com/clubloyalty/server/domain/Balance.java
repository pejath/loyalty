package com.clubloyalty.server.domain;

import javax.persistence.*;

@Entity
@Table(name = "balances")
public class Balance {
    @Id
    private Long id;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Member member;

    private long points;

    public Balance() {
    }

    public Balance(Member m, long p) {
        this.member = m;
        this.points = p;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long p) {
        points = p;
    }
}
