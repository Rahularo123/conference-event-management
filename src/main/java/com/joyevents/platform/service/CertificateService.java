package com.joyevents.platform.service;

import com.joyevents.platform.model.CertificateRecord;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.repository.CertificateRecordRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CertificateService {

    private final CertificateRecordRepository certificateRecordRepository;
    private final DelegateRepository delegateRepository;
    private final QrCodeService qrCodeService;

    public CertificateService(CertificateRecordRepository certificateRecordRepository,
                              DelegateRepository delegateRepository,
                              QrCodeService qrCodeService) {
        this.certificateRecordRepository = certificateRecordRepository;
        this.delegateRepository = delegateRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Issues or retrieves an existing certificate for a doctor.
     */
    @Transactional
    public CertificateRecord getOrCreateCertificate(Delegate delegate, String certificateType) {
        return certificateRecordRepository.findByConferenceAndDelegate(delegate.getConference(), delegate)
                .stream()
                .filter(c -> c.getStatus().equals("ACTIVE"))
                .findFirst()
                .orElseGet(() -> {
                    String certCode = "CERT-" + delegate.getConference().getCode() + "-" + String.format("%04d", delegate.getId());
                    CertificateRecord cert = new CertificateRecord(delegate.getConference(), delegate, certificateType, certCode);
                    return certificateRecordRepository.save(cert);
                });
    }

    /**
     * Generates a high-quality Landscape A4 Certificate PDF.
     */
    public byte[] generateCertificatePdf(CertificateRecord certRecord) {
        try {
            // A4 Landscape orientation for certificate
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 40, 40);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // 1. Certificate Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Font.NORMAL);
            Paragraph title = new Paragraph("CERTIFICATE OF PARTICIPATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(30);
            title.setSpacingAfter(15);
            document.add(title);

            // 2. Subtitle
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
            Paragraph sub = new Paragraph("This is to proudly certify that", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(20);
            document.add(sub);

            // 3. Doctor Full Name (Large Bold)
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26);
            Paragraph name = new Paragraph(certRecord.getDelegate().getFullName().toUpperCase(), nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingAfter(20);
            document.add(name);

            // 4. Conference Body Text
            Delegate d = certRecord.getDelegate();
            String bodyText = String.format("has actively participated as a %s in %s held at %s.",
                    d.getCategory().toUpperCase(),
                    d.getConference().getName(),
                    d.getConference().getVenue());

            Paragraph body = new Paragraph(bodyText, subFont);
            body.setAlignment(Element.ALIGN_CENTER);
            body.setSpacingAfter(40);
            document.add(body);

            // 5. Verification QR code & Verification ID
            byte[] qrBytes = qrCodeService.generateQrCodeImage(certRecord.getCertificateCode(), 100, 100);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.scaleToFit(80, 80);
            document.add(qrImage);

            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 10);
            Paragraph codePara = new Paragraph("Verification Code: " + certRecord.getCertificateCode(), codeFont);
            codePara.setAlignment(Element.ALIGN_CENTER);
            codePara.setSpacingBefore(6);
            document.add(codePara);

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