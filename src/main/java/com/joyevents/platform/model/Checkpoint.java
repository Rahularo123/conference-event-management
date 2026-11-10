package com.joyevents.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "checkpoints")
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    @Column(nullable = false, length = 100)
    private String name; // e.g. "Day 1 Lunch", "Gala Dinner", "Kit Bag"

    @Column(nullable = false, length = 30)
    private String type = "FOOD"; // "FOOD", "KIT", "WORKSHOP"

    private int dayNumber = 1;

    private boolean active = true;

    public Checkpoint() {}

    public Checkpoint(Conference conference, String name, String type, int dayNumber) {
        this.conference = conference;
        this.name = name;
        this.type = type;
        this.dayNumber = dayNumber;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}