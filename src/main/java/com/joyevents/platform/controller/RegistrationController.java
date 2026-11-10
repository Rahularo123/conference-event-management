package com.joyevents.platform.controller;

import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.service.RegistrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registration")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * 1. GET /api/registration/search
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
     * 3. PUT /api/registration/delegate/{id}
     * Updates an existing doctor's details (Fixes phone numbers, typos, category).
     */
    @PutMapping("/delegate/{id}")
    public ResponseEntity<Delegate> updateDelegate(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(required = false, defaultValue = "") String mobile,
            @RequestParam(required = false, defaultValue = "") String institution,
            @RequestParam(defaultValue = "Delegate") String category) {

        Delegate updated = registrationService.updateDelegate(id, fullName, email, mobile, institution, category);
        return ResponseEntity.ok(updated);
    }

    /**
     * 4. DELETE /api/registration/delegate/{id}
     * Deletes a doctor record.
     */
    @DeleteMapping("/delegate/{id}")
    public ResponseEntity<Map<String, Object>> deleteDelegate(@PathVariable Long id) {
        registrationService.deleteDelegate(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Doctor deleted successfully"));
    }

    /**
     * 5. GET /api/registration/badge-pdf/{delegateId}
     * Streams the 4x6 print-ready PDF badge directly.
     */
    @GetMapping("/badge-pdf/{delegateId}")
    public ResponseEntity<byte[]> getBadgePdf(@PathVariable Long delegateId) {
        byte[] pdfBytes = registrationService.printBadgeAndMark(delegateId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "badge-" + delegateId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}