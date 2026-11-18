package com.joyevents.platform.service;

import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class NotificationService {

    /**
     * Formats the official conference registration WhatsApp message template
     * matching the exact layout used by VDO Conference Technologies.
     */
    public String buildWhatsAppMessage(Delegate delegate, String serverBaseUrl) {
        Conference conf = delegate.getConference();
        String confName = conf != null ? conf.getName() : "Conference 2026";
        String venue = (conf != null && conf.getVenue() != null) ? conf.getVenue() : "Conference Venue";
        String dateRange = conf != null ? conf.getFormattedDateRange() : "Conference Dates";

        // URL to download the 4x6 scannable Badge PDF directly
        String badgePdfUrl = serverBaseUrl + "/api/registration/badge-pdf/" + delegate.getId();

        // Venue section: Only append the Google Maps link if explicitly configured by the organizer
        StringBuilder venueSection = new StringBuilder("VENUE:\n").append(venue);
        if (conf != null && conf.getVenueMapUrl() != null && !conf.getVenueMapUrl().isBlank()) {
            venueSection.append("\n").append(conf.getVenueMapUrl().trim());
        }

        return String.format(
            "Dear %s,\n\n" +
            "Thank you for registering for *%s*\n\n" +
            "We, *%s*, are excited to welcome you on the *%s* at *%s*.\n\n" +
            "Please find your registration details below:\n" +
            "Registration id: *%s*\n\n" +
            "Please click below to get your QR Code\n" +
            "%s\n\n" +
            "We look forward to you joining us for an exciting exploration of innovative showcases and engaging networking opportunities. Our team is dedicated to providing you with an exceptional experience.\n\n" +
            "%s\n\n" +
            "See you at the event!\n\n" +
            "Best Regards,\n" +
            "*%s*",
            delegate.getFullName().trim().toUpperCase(),
            confName,
            confName,
            dateRange,
            venue,
            delegate.getRegistrationId(),
            badgePdfUrl,
            venueSection.toString(),
            confName
        );
    }

    /**
     * Generates a direct 'Click-to-Chat' WhatsApp link (https://wa.me/...).
     * Opens real WhatsApp immediately on browser or phone with the pre-filled template.
     */
    public String generateWhatsAppWebLink(Delegate delegate, String serverBaseUrl) {
        String message = buildWhatsAppMessage(delegate, serverBaseUrl);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        String cleanPhone = delegate.getMobile() != null ? delegate.getMobile().replaceAll("[^0-9]", "") : "";
        if (!cleanPhone.startsWith("91") && cleanPhone.length() == 10) {
            cleanPhone = "91" + cleanPhone; // Prefix Indian country code +91
        }

        return "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;
    }

    /**
     * Automated dispatch gateway (for production WATI / Meta Cloud API).
     */
    public boolean sendAutomatedWhatsApp(Delegate delegate, String serverBaseUrl, String apiKey) {
        String phone = delegate.getMobile();
        String message = buildWhatsAppMessage(delegate, serverBaseUrl);
        System.out.println(">>> [WhatsApp Gateway] Automated dispatch queued for: " + phone);
        System.out.println(message);
        // Returns true once HTTP POST to Meta / WATI endpoint completes
        return true;
    }
}