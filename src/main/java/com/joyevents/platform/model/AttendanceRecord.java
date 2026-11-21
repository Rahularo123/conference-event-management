package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"conference_id", "delegate_id", "day_number"})
})
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delegate_id", nullable = false)
    private Delegate delegate;

    @Column(name = "day_number", nullable = false)
    private int dayNumber; // 1, 2, or 3

    @Column(nullable = false)
    private boolean attended = false;

    private LocalDateTime checkInTime;

    @Column(name = "photo_data", columnDefinition = "TEXT")
    private String photoData; // Base64 snapshot from webcam proof

    public AttendanceRecord() {}

    public AttendanceRecord(Conference conference, Delegate delegate, int dayNumber, boolean attended) {
        this.conference = conference;
        this.delegate = delegate;
        this.dayNumber = dayNumber;
        this.attended = attended;
        if (attended) {
            this.checkInTime = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public Delegate getDelegate() { return delegate; }
    public void setDelegate(Delegate delegate) { this.delegate = delegate; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { 
        this.attended = attended;
        if (attended && this.checkInTime == null) {
            this.checkInTime = LocalDateTime.now();
        }
    }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public String getPhotoData() { return photoData; }
    public void setPhotoData(String photoData) { this.photoData = photoData; }
}