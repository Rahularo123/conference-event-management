package com.joyevents.platform.service;

import com.joyevents.platform.model.CertificateRecord;
import com.joyevents.platform.model.CertificateTemplate;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.RoomEntry;
import com.joyevents.platform.repository.CertificateRecordRepository;
import com.joyevents.platform.repository.CertificateTemplateRepository;
import com.joyevents.platform.repository.RoomEntryRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificateService {

    private final CertificateRecordRepository certificateRecordRepository;
    private final CertificateTemplateRepository templateRepository;
    private final RoomEntryRepository roomEntryRepository;
    private final QrCodeService qrCodeService;

    public CertificateService(CertificateRecordRepository certificateRecordRepository,
                              CertificateTemplateRepository templateRepository,
                              RoomEntryRepository roomEntryRepository,
                              QrCodeService qrCodeService) {
        this.certificateRecordRepository = certificateRecordRepository;
        this.templateRepository = templateRepository;
        this.roomEntryRepository = roomEntryRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Issues or retrieves an existing certificate record for an attendee.
     */
    @Transactional
    public CertificateRecord getOrCreateCertificate(Delegate delegate, String certificateType) {
        return certificateRecordRepository.findByConferenceAndDelegate(delegate.getConference(), delegate)
                .stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .findFirst()
                .orElseGet(() -> {
                    String certCode = "CERT-" + delegate.getConference().getCode() + "-" + String.format("%04d", delegate.getId());
                    CertificateRecord cert = new CertificateRecord(delegate.getConference(), delegate, certificateType, certCode);
                    return certificateRecordRepository.save(cert);
                });
    }

    /**
     * Gets or creates default coordinate template for an event.
     */
    public CertificateTemplate getTemplate(String conferenceCode) {
        return templateRepository.findByConferenceCode(conferenceCode)
                .orElseGet(() -> new CertificateTemplate(conferenceCode));
    }

    @Transactional
    public CertificateTemplate saveTemplate(CertificateTemplate template) {
        return templateRepository.save(template);
    }

    /**
     * Generates a Landscape A4 Certificate PDF using exact X, Y coordinate placement.
     */
    public byte[] generateCertificatePdf(CertificateRecord certRecord) {
        try {
            Delegate delegate = certRecord.getDelegate();
            String confCode = delegate.getConference().getCode();
            CertificateTemplate tpl = getTemplate(confCode);

            // 1. Calculate verified CME credit hours from unique ENTRY room logs (ignore EXIT scans)
            List<RoomEntry> roomEntries = roomEntryRepository.findByDelegateOrderByScannedAtDesc(delegate)
                    .stream()
                    .filter(e -> !"EXIT".equalsIgnoreCase(e.getScanType()))
                    .toList();
            int cmeSessions = roomEntries.size();
            double cmeHours = roomEntries.stream()
                    .mapToDouble(e -> (e.getRoom() != null && e.getRoom().getCmeCredits() != null) ? e.getRoom().getCmeCredits() : 1.5)
                    .sum();
            cmeHours = Math.round(cmeHours * 10.0) / 10.0;

            // 2. Setup A4 Landscape Document (842 x 595 points)
            Rectangle pageSize = PageSize.A4.rotate();
            Document document = new Document(pageSize, 0, 0, 0, 0);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();
            PdfContentByte cb = writer.getDirectContent();

            // 3. Background Artwork Image (if uploaded) or Elegant Certificate Border
            if (tpl.getBackgroundImagePath() != null && new File(tpl.getBackgroundImagePath()).exists()) {
                Image bgImage = Image.getInstance(tpl.getBackgroundImagePath());
                bgImage.setAbsolutePosition(0, 0);
                bgImage.scaleAbsolute(pageSize.getWidth(), pageSize.getHeight());
                cb.addImage(bgImage);
            } else {
                // Default Double Border (Gold/Navy)
                cb.setColorStroke(new Color(15, 23, 42)); // Navy
                cb.setLineWidth(3f);
                cb.rectangle(20, 20, pageSize.getWidth() - 40, pageSize.getHeight() - 40);
                cb.stroke();

                cb.setColorStroke(new Color(180, 83, 9)); // Amber / Gold
                cb.setLineWidth(1.5f);
                cb.rectangle(26, 26, pageSize.getWidth() - 52, pageSize.getHeight() - 52);
                cb.stroke();
            }

            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfRegular = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // 4. Header Titles (if no background artwork covers it)
            if (tpl.getBackgroundImagePath() == null) {
                cb.beginText();
                cb.setFontAndSize(bfBold, 24);
                cb.setColorFill(new Color(15, 23, 42));
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "CERTIFICATE OF PARTICIPATION", pageSize.getWidth() / 2, 490, 0);

                cb.setFontAndSize(bfRegular, 12);
                cb.setColorFill(new Color(100, 116, 139));
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "This is to proudly certify that", pageSize.getWidth() / 2, 455, 0);
                cb.endText();
            }

            // 5. Doctor Name (Exact X, Y from template)
            cb.beginText();
            cb.setFontAndSize(bfBold, tpl.getNameFontSize());
            cb.setColorFill(new Color(15, 23, 42));
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, delegate.getFullName().toUpperCase(), tpl.getNameX(), tpl.getNameY(), 0);

            // 6. Registration ID & Category
            cb.setFontAndSize(bfRegular, tpl.getRegIdFontSize());
            cb.setColorFill(new Color(71, 85, 105));
            String regDetails = "Registration ID: " + delegate.getRegistrationId() + "  |  Category: " + delegate.getCategory().toUpperCase();
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, regDetails, tpl.getRegIdX(), tpl.getRegIdY(), 0);

            // 7. Event & Venue Body Text
            String confLine = "has attended " + delegate.getConference().getName() + " held at " + delegate.getConference().getVenue();
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, confLine, tpl.getRegIdX(), tpl.getRegIdY() - 25, 0);

            // 8. CME Credit Hours Line (if earned)
            if (cmeHours > 0) {
                cb.setFontAndSize(bfBold, tpl.getCmeFontSize());
                cb.setColorFill(new Color(2, 132, 199)); // Medical Blue
                String cmeText = "Awarded " + cmeHours + " CME Credit Hours (" + cmeSessions + " Scientific Sessions Attended)";
                cb.showTextAligned(PdfContentByte.ALIGN_CENTER, cmeText, tpl.getCmeX(), tpl.getCmeY(), 0);
            }
            cb.endText();

            // 9. Digital Verification QR Code (Exact X, Y from template)
            byte[] qrBytes = qrCodeService.generateQrCodeImage(certRecord.getCertificateCode(), 150, 150);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAbsolutePosition(tpl.getQrX(), tpl.getQrY());
            qrImage.scaleAbsolute(tpl.getQrSize(), tpl.getQrSize());
            cb.addImage(qrImage);

            // 10. Verification Code Label below QR code
            cb.beginText();
            cb.setFontAndSize(bfRegular, 8);
            cb.setColorFill(new Color(100, 116, 139));
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, certRecord.getCertificateCode(), tpl.getQrX() + (tpl.getQrSize() / 2), tpl.getQrY() - 12, 0);
            cb.endText();

            document.close();

            // Mark as downloaded
            certRecord.setDownloadedOnline(true);
            certRecord.setDownloadedAt(LocalDateTime.now());
            certificateRecordRepository.save(certRecord);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }
}