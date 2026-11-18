package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "conferences")
public class Conference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    private String venue;

    @Column(name = "venue_map_url")
    private String venueMapUrl;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 1000)
    private String description;

    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Conference() {}

    public Conference(String code, String name, String venue, LocalDate startDate, LocalDate endDate) {
        this(code, name, venue, null, startDate, endDate);
    }

    public Conference(String code, String name, String venue, String venueMapUrl, LocalDate startDate, LocalDate endDate) {
        this.code = code;
        this.name = name;
        this.venue = venue;
        this.venueMapUrl = venueMapUrl;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Formats start and end dates into readable text, e.g. "24 October 2026 to 25 October 2026"
     */
    public String getFormattedDateRange() {
        if (startDate == null) return "Upcoming Dates";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
        if (endDate == null || startDate.isEqual(endDate)) {
            return startDate.format(formatter);
        }
        return startDate.format(formatter) + " to " + endDate.format(formatter);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getVenueMapUrl() { return venueMapUrl; }
    public void setVenueMapUrl(String venueMapUrl) { this.venueMapUrl = venueMapUrl; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}