package com.clubloyalty.server.domain;

import javax.persistence.*;

@Entity
@Table(name = "rewards")
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private int cost;
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        title = t;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        description = d;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int c) {
        cost = c;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean a) {
        active = a;
    }
}
