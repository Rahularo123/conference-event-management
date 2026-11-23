package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eposters")
public class EPoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String conferenceCode; // e.g. PHO26

    @Column(nullable = false, length = 30)
    private String posterNumber; // e.g. EP-101

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 150)
    private String primaryAuthor;

    @Column(length = 300)
    private String coAuthors;

    @Column(length = 200)
    private String institution; // e.g. PGIMER Chandigarh

    @Column(nullable = false, length = 100)
    private String track; // e.g. "Leukemia & Lymphoma", "Stem Cell Transplant", "Solid Tumors"

    @Column(length = 4000)
    private String abstractText;

    private String fileUrl; // Path to digital poster PDF or image

    private int viewCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public EPoster() {}

    public EPoster(String conferenceCode, String posterNumber, String title, String primaryAuthor, 
                   String coAuthors, String institution, String track, String abstractText, String fileUrl) {
        this.conferenceCode = conferenceCode;
        this.posterNumber = posterNumber;
        this.title = title;
        this.primaryAuthor = primaryAuthor;
        this.coAuthors = coAuthors;
        this.institution = institution;
        this.track = track;
        this.abstractText = abstractText;
        this.fileUrl = fileUrl;
        this.viewCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConferenceCode() { return conferenceCode; }
    public void setConferenceCode(String conferenceCode) { this.conferenceCode = conferenceCode; }

    public String getPosterNumber() { return posterNumber; }
    public void setPosterNumber(String posterNumber) { this.posterNumber = posterNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrimaryAuthor() { return primaryAuthor; }
    public void setPrimaryAuthor(String primaryAuthor) { this.primaryAuthor = primaryAuthor; }

    public String getCoAuthors() { return coAuthors; }
    public void setCoAuthors(String coAuthors) { this.coAuthors = coAuthors; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getTrack() { return track; }
    public void setTrack(String track) { this.track = track; }

    public String getAbstractText() { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
