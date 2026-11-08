package com.joyevents.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "category_rules")
public class CategoryRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many category rules belong to One Conference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    @Column(nullable = false, length = 50)
    private String categoryName; // e.g. "Delegate", "Faculty", "Organizer", "VIP"

    private int kitsAllowed = 1;
    private int lunchesAllowed = 1;
    private boolean certificateEligible = true;

    // Default constructor
    public CategoryRule() {}

    // Parameterized constructor
    public CategoryRule(Conference conference, String categoryName, int kitsAllowed, int lunchesAllowed, boolean certificateEligible) {
        this.conference = conference;
        this.categoryName = categoryName;
        this.kitsAllowed = kitsAllowed;
        this.lunchesAllowed = lunchesAllowed;
        this.certificateEligible = certificateEligible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getKitsAllowed() { return kitsAllowed; }
    public void setKitsAllowed(int kitsAllowed) { this.kitsAllowed = kitsAllowed; }

    public int getLunchesAllowed() { return lunchesAllowed; }
    public void setLunchesAllowed(int lunchesAllowed) { this.lunchesAllowed = lunchesAllowed; }

    public boolean isCertificateEligible() { return certificateEligible; }
    public void setCertificateEligible(boolean certificateEligible) { this.certificateEligible = certificateEligible; }
}