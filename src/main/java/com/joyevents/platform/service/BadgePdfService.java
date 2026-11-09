package com.joyevents.platform.service;

import com.joyevents.platform.model.Delegate;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class BadgePdfService {

    private final QrCodeService qrCodeService;

    // Spring automatically injects our QrCodeService here (Dependency Injection)
    public BadgePdfService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    /**
     * Generates a 4x6 inch portrait PDF badge for a delegate.
     */
    public byte[] generateBadgePdf(Delegate delegate) {
        try {
            // 4 inches x 6 inches (1 inch = 72 points in PDF standard)
            Rectangle pageSize = new RectangleReadOnly(288, 432);
            Document document = new Document(pageSize, 18, 18, 20, 20); // 18pt margins

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // 1. Spacing to align with the bottom white area of pre-printed cards
            Paragraph spacer = new Paragraph(" ");
            spacer.setSpacingBefore(140); // Skips the top pre-printed conference logo area
            document.add(spacer);

            // 2. Doctor / Attendee Name (Bold, Auto-sized)
            int nameFontSize = delegate.getFullName().length() > 20 ? 14 : 18;
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, nameFontSize);
            Paragraph nameParagraph = new Paragraph(delegate.getFullName().toUpperCase(), nameFont);
            nameParagraph.setAlignment(Element.ALIGN_CENTER);
            nameParagraph.setSpacingAfter(8);
            document.add(nameParagraph);

            // 3. The QR Code (generated using our QrCodeService)
            byte[] qrBytes = qrCodeService.generateQrCodeImage(delegate.getQrToken(), 140, 140);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.scaleToFit(110, 110);
            document.add(qrImage);

            // 4. Registration ID below QR code (e.g. PHOS-00110)
            Font regIdFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph regIdPara = new Paragraph(delegate.getRegistrationId(), regIdFont);
            regIdPara.setAlignment(Element.ALIGN_CENTER);
            regIdPara.setSpacingBefore(4);
            regIdPara.setSpacingAfter(10);
            document.add(regIdPara);

            // 5. Category Banner (e.g. DELEGATE, FACULTY, CREW)
            Font categoryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Paragraph categoryPara = new Paragraph(delegate.getCategory().toUpperCase(), categoryFont);
            categoryPara.setAlignment(Element.ALIGN_CENTER);
            document.add(categoryPara);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate badge PDF for: " + delegate.getRegistrationId(), e);
        }
    }
}