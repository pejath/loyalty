package com.clubloyalty.server.domain;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 1000)
    private String description;
    @Column(nullable = false)
    private Instant startAt;
    @Column(nullable = false)
    private Instant endAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;
    private Integer pointsAmount;
    private String notificationTitle;
    private String notificationMessage;
    private boolean executed;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getPointsAmount() {
        return pointsAmount;
    }

    public void setPointsAmount(Integer pointsAmount) {
        this.pointsAmount = pointsAmount;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum ActionType {AWARD_POINTS, SEND_NOTIFICATION}
}
