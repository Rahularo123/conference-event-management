package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delegates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"conference_id", "registration_id"}),
    @UniqueConstraint(columnNames = {"qr_token"})
})
public class Delegate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many delegates belong to One Conference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    @Column(name = "registration_id", nullable = false, length = 50)
    private String registrationId; // e.g. "PHOS-00110"

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String mobile;

    @Column(length = 200)
    private String institution; // Hospital or Medical College

    @Column(length = 100)
    private String city;

    @Column(nullable = false, length = 50)
    private String category; // "Delegate", "Faculty", "Organizer", "VIP"

    @Column(name = "qr_token", nullable = false, unique = true, length = 64)
    private String qrToken;

    private boolean badgePrinted = false;
    private LocalDateTime badgePrintTimestamp;

    private String registrationSource = "ONSITE"; // "ONSITE", "ONLINE", "IMPORTED"
    private LocalDateTime registeredAt = LocalDateTime.now();

    // Default constructor generates a secure random QR token automatically
    public Delegate() {
        this.qrToken = UUID.randomUUID().toString().replace("-", "");
    }

    // Parameterized constructor
    public Delegate(Conference conference, String registrationId, String fullName, String email, String mobile, String institution, String category) {
        this();
        this.conference = conference;
        this.registrationId = registrationId;
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.institution = institution;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public boolean isBadgePrinted() { return badgePrinted; }
    public void setBadgePrinted(boolean badgePrinted) { this.badgePrinted = badgePrinted; }

    public LocalDateTime getBadgePrintTimestamp() { return badgePrintTimestamp; }
    public void setBadgePrintTimestamp(LocalDateTime badgePrintTimestamp) { this.badgePrintTimestamp = badgePrintTimestamp; }

    public String getRegistrationSource() { return registrationSource; }
    public void setRegistrationSource(String registrationSource) { this.registrationSource = registrationSource; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}