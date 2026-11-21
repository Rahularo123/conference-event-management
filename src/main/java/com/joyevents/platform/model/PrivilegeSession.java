package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "privilege_sessions")
public class PrivilegeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conferenceCode; // e.g. PHO26

    @Column(nullable = false)
    private String name; // e.g. "Day 1 - Lunch", "Delegate Kit Collection"

    @Column(nullable = false)
    private String privilegeType; // FOOD, KIT, WORKSHOP, BANQUET

    private Integer dayNumber; // 1, 2, 3

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 500)
    private String allowedCategories; // Comma-separated: "FACULTY,DELEGATE,PG_STUDENT" (empty = all)

    private boolean active = true;

    public PrivilegeSession() {}

    public PrivilegeSession(String conferenceCode, String name, String privilegeType, 
                            Integer dayNumber, LocalDateTime startTime, LocalDateTime endTime, 
                            String allowedCategories) {
        this.conferenceCode = conferenceCode;
        this.name = name;
        this.privilegeType = privilegeType;
        this.dayNumber = dayNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.allowedCategories = allowedCategories;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConferenceCode() { return conferenceCode; }
    public void setConferenceCode(String conferenceCode) { this.conferenceCode = conferenceCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrivilegeType() { return privilegeType; }
    public void setPrivilegeType(String privilegeType) { this.privilegeType = privilegeType; }

    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getAllowedCategories() { return allowedCategories; }
    public void setAllowedCategories(String allowedCategories) { this.allowedCategories = allowedCategories; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}