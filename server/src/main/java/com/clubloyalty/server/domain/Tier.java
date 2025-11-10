package com.clubloyalty.server.domain;

import javax.persistence.*;

@Entity
@Table(name = "tiers")
public class Tier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private long threshold;

    public Tier() {
    }

    public Tier(String n, long t) {
        name = n;
        threshold = t;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long t) {
        threshold = t;
    }
}
