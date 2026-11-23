
package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_entries", indexes = {
    @Index(name = "idx_room_entry_lookup", columnList = "room_id, delegate_id, session_name")
})
public class RoomEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ConferenceRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_id", nullable = false)
    private Delegate delegate;

    @Column(name = "session_name", length = 200)
    private String sessionName; // Title of lecture underway at entry time

    @Column(nullable = false)
    private LocalDateTime scannedAt = LocalDateTime.now();

    @Column(length = 20)
    private String scanType = "ENTRY"; // "ENTRY" or "EXIT"

    @Column(length = 50)
    private String operatorUsername;

    public RoomEntry() {}

    public RoomEntry(ConferenceRoom room, Delegate delegate, String sessionName, String scanType, String operatorUsername) {
        this.room = room;
        this.delegate = delegate;
        this.sessionName = sessionName;
        this.scanType = scanType != null ? scanType : "ENTRY";
        this.operatorUsername = operatorUsername;
        this.scannedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ConferenceRoom getRoom() { return room; }
    public void setRoom(ConferenceRoom room) { this.room = room; }

    public Delegate getDelegate() { return delegate; }
    public void setDelegate(Delegate delegate) { this.delegate = delegate; }

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public String getScanType() { return scanType; }
    public void setScanType(String scanType) { this.scanType = scanType; }

    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }
}