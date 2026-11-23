package com.joyevents.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conference_rooms")
public class ConferenceRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conferenceCode; // e.g. "PHO26"

    @Column(nullable = false)
    private String name; // e.g. "Main Auditorium Hall A", "Bhargava Auditorium"

    @Column(nullable = false)
    private int capacity; // Maximum seating capacity (e.g. 350)

    @Column(length = 200)
    private String currentSession; // e.g. "Keynote: Pediatric Leukemia Frontiers"

    private boolean active = true;

    public ConferenceRoom() {}

    public ConferenceRoom(String conferenceCode, String name, int capacity, String currentSession) {
        this.conferenceCode = conferenceCode;
        this.name = name;
        this.capacity = capacity;
        this.currentSession = currentSession;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConferenceCode() { return conferenceCode; }
    public void setConferenceCode(String conferenceCode) { this.conferenceCode = conferenceCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getCurrentSession() { return currentSession; }
    public void setCurrentSession(String currentSession) { this.currentSession = currentSession; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}