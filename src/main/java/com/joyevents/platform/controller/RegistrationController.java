package com.joyevents.platform.controller;

import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.service.RegistrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    // Dependency Injection: Spring plugs in RegistrationService automatically
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * 1. GET /api/registration/search?conferenceCode=PHO26&query=Sharma
     * Returns matching delegates for the search bar.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Delegate>> searchDelegates(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(required = false, defaultValue = "") String query) {

        List<Delegate> delegates = registrationService.searchDelegates(conferenceCode, query);
        return ResponseEntity.ok(delegates);
    }

    /**
     * 2. POST /api/registration/register-onsite
     * Registers a walk-in doctor on the spot.
     */
    @PostMapping("/register-onsite")
    public ResponseEntity<Delegate> registerOnsite(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam String fullName,
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(required = false, defaultValue = "") String mobile,
            @RequestParam(required = false, defaultValue = "") String institution,
            @RequestParam(defaultValue = "Delegate") String category) {

        Delegate newDelegate = registrationService.registerOnsiteDelegate(
                conferenceCode, fullName, email, mobile, institution, category);

        return ResponseEntity.ok(newDelegate);
    }

    /**
     * 3. GET /api/registration/badge-pdf/{delegateId}
     * Streams the 4x6 print-ready PDF badge directly to the printer / browser dialog.
     */
    @GetMapping("/badge-pdf/{delegateId}")
    public ResponseEntity<byte[]> getBadgePdf(@PathVariable Long delegateId) {
        // Generates PDF and marks badge as printed in database
        byte[] pdfBytes = registrationService.printBadgeAndMark(delegateId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // "inline" tells the browser to display and print the PDF rather than force-downloading it
        headers.setContentDispositionFormData("inline", "badge-" + delegateId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}