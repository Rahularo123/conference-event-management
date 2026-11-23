package com.joyevents.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "certificate_templates")
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String conferenceCode; // e.g. "PHO26"

    @Column(length = 255)
    private String backgroundImagePath; // Path or URL to pre-designed certificate background

    // Doctor Name Coordinates & Font Size (in A4 points: 842 x 595 landscape)
    private float nameX = 421f; // Center of A4 landscape
    private float nameY = 320f;
    private int nameFontSize = 26;

    // Registration ID Coordinates
    private float regIdX = 421f;
    private float regIdY = 260f;
    private int regIdFontSize = 12;

    // CME Credit Hours Coordinates
    private float cmeX = 421f;
    private float cmeY = 220f;
    private int cmeFontSize = 12;

    // Verification QR Code Coordinates
    private float qrX = 720f;
    private float qrY = 120f;
    private float qrSize = 70f;

    private boolean active = true;

    public CertificateTemplate() {}

    public CertificateTemplate(String conferenceCode) {
        this.conferenceCode = conferenceCode;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConferenceCode() { return conferenceCode; }
    public void setConferenceCode(String conferenceCode) { this.conferenceCode = conferenceCode; }

    public String getBackgroundImagePath() { return backgroundImagePath; }
    public void setBackgroundImagePath(String backgroundImagePath) { this.backgroundImagePath = backgroundImagePath; }

    public float getNameX() { return nameX; }
    public void setNameX(float nameX) { this.nameX = nameX; }

    public float getNameY() { return nameY; }
    public void setNameY(float nameY) { this.nameY = nameY; }

    public int getNameFontSize() { return nameFontSize; }
    public void setNameFontSize(int nameFontSize) { this.nameFontSize = nameFontSize; }

    public float getRegIdX() { return regIdX; }
    public void setRegIdX(float regIdX) { this.regIdX = regIdX; }

    public float getRegIdY() { return regIdY; }
    public void setRegIdY(float regIdY) { this.regIdY = regIdY; }

    public int getRegIdFontSize() { return regIdFontSize; }
    public void setRegIdFontSize(int regIdFontSize) { this.regIdFontSize = regIdFontSize; }

    public float getCmeX() { return cmeX; }
    public void setCmeX(float cmeX) { this.cmeX = cmeX; }

    public float getCmeY() { return cmeY; }
    public void setCmeY(float cmeY) { this.cmeY = cmeY; }

    public int getCmeFontSize() { return cmeFontSize; }
    public void setCmeFontSize(int cmeFontSize) { this.cmeFontSize = cmeFontSize; }

    public float getQrX() { return qrX; }
    public void setQrX(float qrX) { this.qrX = qrX; }

    public float getQrY() { return qrY; }
    public void setQrY(float qrY) { this.qrY = qrY; }

    public float getQrSize() { return qrSize; }
    public void setQrSize(float qrSize) { this.qrSize = qrSize; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}