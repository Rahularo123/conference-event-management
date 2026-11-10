package com.joyevents.platform.service;

import com.joyevents.platform.model.Delegate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class NotificationService {

    /**
     * Formats the official conference registration WhatsApp message template.
     */
    public String buildWhatsAppMessage(Delegate delegate, String serverBaseUrl) {
        String badgePdfUrl = serverBaseUrl + "/api/registration/badge-pdf/" + delegate.getId();

        return String.format(
            "Dear %s,\n\n" +
            "Thank you for registering for *%s*.\n\n" +
            "We are excited to welcome you to %s!\n\n" +
            "Please find your registration details below:\n" +
            "Registration ID: *%s*\n" +
            "Category: *%s*\n\n" +
            "Please click below to get your badge and QR Code:\n" +
            "%s\n\n" +
            "See you at the event!\n\n" +
            "Best Regards,\n" +
            "*%s Team*",
            delegate.getFullName().toUpperCase(),
            delegate.getConference().getName(),
            delegate.getConference().getVenue(),
            delegate.getRegistrationId(),
            delegate.getCategory(),
            badgePdfUrl,
            delegate.getConference().getName()
        );
    }

    /**
     * Generates a direct 'Click-to-Chat' WhatsApp Web link (https://wa.me/...).
     * Opens real WhatsApp on any device with the message pre-filled!
     */
    public String generateWhatsAppWebLink(Delegate delegate, String serverBaseUrl) {
        String message = buildWhatsAppMessage(delegate, serverBaseUrl);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        String cleanPhone = delegate.getMobile() != null ? delegate.getMobile().replaceAll("[^0-9]", "") : "";
        if (!cleanPhone.startsWith("91") && cleanPhone.length() == 10) {
            cleanPhone = "91" + cleanPhone; // Default to India country code +91
        }

        return "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;
    }

    /**
     * Placeholder method for WATI / Meta Cloud API (Production).
     * When your brother buys the WATI subscription, this method fires the automated HTTP request.
     */
    public boolean sendAutomatedWatiWhatsApp(String phone, String templateMessage, String apiKey) {
        System.out.println(">>> [Production Gateway] Dispatching automated WhatsApp to: " + phone);
        // Will perform HTTP POST to WATI API endpoint using apiKey
        return true;
    }
}