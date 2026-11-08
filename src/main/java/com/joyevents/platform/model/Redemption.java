package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "redemptions", indexes = {
    @Index(name = "idx_redemption_lookup", columnList = "delegate_id, item_type, day_number")
})
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_id", nullable = false)
    private Delegate delegate;

    @Column(name = "item_type", nullable = false, length = 30)
    private String itemType; // "LUNCH", "KIT", "WORKSHOP"

    @Column(name = "day_number")
    private int dayNumber = 1; // 1 for Day 1, 2 for Day 2, etc.

    @Column(nullable = false)
    private LocalDateTime redeemedAt = LocalDateTime.now();

    @Column(length = 50)
    private String counterName; // e.g. "Lunch Counter 1", "Kit Desk"

    @Column(length = 50)
    private String operatorUsername;

    @Column(nullable = false, length = 30)
    private String status = "SUCCESS"; // "SUCCESS", "REJECTED"

    @Column(length = 200)
    private String remarks; // e.g. "Scan verified", "Already claimed at 1:15 PM"

    public Redemption() {}

    public Redemption(Conference conference, Delegate delegate, String itemType, int dayNumber, String counterName, String operatorUsername) {
        this.conference = conference;
        this.delegate = delegate;
        this.itemType = itemType;
        this.dayNumber = dayNumber;
        this.counterName = counterName;
        this.operatorUsername = operatorUsername;
        this.redeemedAt = LocalDateTime.now();
        this.status = "SUCCESS";
        this.remarks = "Scan verified";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public Delegate getDelegate() { return delegate; }
    public void setDelegate(Delegate delegate) { this.delegate = delegate; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public LocalDateTime getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(LocalDateTime redeemedAt) { this.redeemedAt = redeemedAt; }

    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }

    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}