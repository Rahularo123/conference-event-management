package com.joyevents.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_records", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"certificate_code"})
})
public class CertificateRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_id", nullable = false)
    private Conference conference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegate_id", nullable = false)
    private Delegate delegate;

    @Column(nullable = false, length = 100)
    private String certificateType = "Certificate of Participation";

    @Column(name = "certificate_code", nullable = false, length = 64)
    private String certificateCode; // Unique verification code e.g. "CERT-PHO-001"

    private boolean printedOnsite = false;
    private LocalDateTime printedAt;

    private boolean downloadedOnline = false;
    private LocalDateTime downloadedAt;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // "ACTIVE", "REVOKED", "WITHHELD"

    private LocalDateTime issuedAt = LocalDateTime.now();

    public CertificateRecord() {}

    public CertificateRecord(Conference conference, Delegate delegate, String certificateType, String certificateCode) {
        this.conference = conference;
        this.delegate = delegate;
        this.certificateType = certificateType;
        this.certificateCode = certificateCode;
        this.issuedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conference getConference() { return conference; }
    public void setConference(Conference conference) { this.conference = conference; }

    public Delegate getDelegate() { return delegate; }
    public void setDelegate(Delegate delegate) { this.delegate = delegate; }

    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }

    public String getCertificateCode() { return certificateCode; }
    public void setCertificateCode(String certificateCode) { this.certificateCode = certificateCode; }

    public boolean isPrintedOnsite() { return printedOnsite; }
    public void setPrintedOnsite(boolean printedOnsite) { this.printedOnsite = printedOnsite; }

    public LocalDateTime getPrintedAt() { return printedAt; }
    public void setPrintedAt(LocalDateTime printedAt) { this.printedAt = printedAt; }

    public boolean isDownloadedOnline() { return downloadedOnline; }
    public void setDownloadedOnline(boolean downloadedOnline) { this.downloadedOnline = downloadedOnline; }

    public LocalDateTime getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(LocalDateTime downloadedAt) { this.downloadedAt = downloadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}