package com.joyevents.platform.controller;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.CertificateRecord;
import com.joyevents.platform.model.CertificateTemplate;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.RoomEntry;
import com.joyevents.platform.repository.CategoryRuleRepository;
import com.joyevents.platform.repository.CertificateRecordRepository;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.RoomEntryRepository;
import com.joyevents.platform.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "*")
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateRecordRepository certificateRecordRepository;
    private final DelegateRepository delegateRepository;
    private final ConferenceRepository conferenceRepository;
    private final CategoryRuleRepository categoryRuleRepository;
    private final RoomEntryRepository roomEntryRepository;

    public CertificateController(CertificateService certificateService,
                                 CertificateRecordRepository certificateRecordRepository,
                                 DelegateRepository delegateRepository,
                                 ConferenceRepository conferenceRepository,
                                 CategoryRuleRepository categoryRuleRepository,
                                 RoomEntryRepository roomEntryRepository) {
        this.certificateService = certificateService;
        this.certificateRecordRepository = certificateRecordRepository;
        this.delegateRepository = delegateRepository;
        this.conferenceRepository = conferenceRepository;
        this.categoryRuleRepository = categoryRuleRepository;
        this.roomEntryRepository = roomEntryRepository;
    }

    /**
     * 1. GET /api/certificate/lookup?conferenceCode=PHO26&term=PHO26-0001
     * Looks up attendee by Registration ID or registered Mobile number.
     * Computes CME credits based on verified auditorium door scans.
     */
    @GetMapping("/lookup")
    public ResponseEntity<?> lookupCertificate(
            @RequestParam String conferenceCode,
            @RequestParam String term) {

        String query = (term != null) ? term.trim() : "";
        if (query.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please enter a valid Registration ID or Mobile Number."
            ));
        }

        Optional<Conference> confOpt = conferenceRepository.findByCode(conferenceCode.trim().toUpperCase());
        if (confOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Conference with code '" + conferenceCode + "' not found."
            ));
        }

        Conference conference = confOpt.get();

        // 1. Direct match by Registration ID
        Optional<Delegate> delegateOpt = delegateRepository.findByConferenceAndRegistrationIdIgnoreCase(conference, query);

        // 2. Fallback to omni-search (matches mobile number or name)
        Delegate delegate = delegateOpt.orElseGet(() -> {
            List<Delegate> matches = delegateRepository.searchDelegates(conference, query);
            return matches.isEmpty() ? null : matches.get(0);
        });

        if (delegate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "No attendee record found matching '" + query + "'. Please check your Registration ID or Mobile."
            ));
        }

        // Check category eligibility rule
        Optional<CategoryRule> ruleOpt = categoryRuleRepository
                .findByConferenceAndCategoryNameIgnoreCase(delegate.getConference(), delegate.getCategory());

        if (ruleOpt.isPresent() && !ruleOpt.get().isCertificateEligible()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "eligible", false,
                    "fullName", delegate.getFullName(),
                    "category", delegate.getCategory(),
                    "conferenceName", delegate.getConference().getName(),
                    "message", "Certificates are not applicable for category: " + delegate.getCategory()
            ));
        }

        // Issue or fetch existing certificate record
        CertificateRecord record = certificateService.getOrCreateCertificate(delegate, "PARTICIPATION");

        // Calculate verified CME scientific sessions attended from auditorium door scans
        List<RoomEntry> roomEntries = roomEntryRepository.findByDelegateOrderByScannedAtDesc(delegate);
        int cmeSessionsAttended = roomEntries.size();
        double cmeCreditHours = roomEntries.stream()
                .mapToDouble(entry -> (entry.getRoom() != null && entry.getRoom().getCmeCredits() != null)
                        ? entry.getRoom().getCmeCredits()
                        : 1.5)
                .sum();
        cmeCreditHours = Math.round(cmeCreditHours * 10.0) / 10.0;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("eligible", true);
        response.put("delegateId", delegate.getId());
        response.put("fullName", delegate.getFullName());
        response.put("registrationId", delegate.getRegistrationId());
        response.put("category", delegate.getCategory());
        response.put("institution", delegate.getInstitution() != null ? delegate.getInstitution() : "");
        response.put("conferenceName", delegate.getConference().getName());
        response.put("conferenceCode", delegate.getConference().getCode());
        response.put("certificateCode", record.getCertificateCode());
        response.put("downloadUrl", "/api/certificate/download/" + record.getCertificateCode());
        response.put("downloadedOnline", record.isDownloadedOnline());
        response.put("cmeSessionsAttended", cmeSessionsAttended);
        response.put("cmeCreditHours", cmeCreditHours);

        return ResponseEntity.ok(response);
    }

    /**
     * 2. GET /api/certificate/download/{certCode}
     * Streams the generated Landscape A4 PDF directly to the browser.
     */
    @GetMapping("/download/{certCode}")
    public ResponseEntity<?> downloadCertificatePdf(@PathVariable String certCode) {
        Optional<CertificateRecord> certOpt = certificateRecordRepository.findByCertificateCode(certCode);
        if (certOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Certificate not found for verification code: " + certCode);
        }

        CertificateRecord cert = certOpt.get();
        if ("REVOKED".equalsIgnoreCase(cert.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This certificate has been revoked by event administration.");
        }

        byte[] pdfBytes = certificateService.generateCertificatePdf(cert);

        // Mark delegate certificate as downloaded
        if (cert.getDelegate() != null && !cert.getDelegate().isCertificateDownloaded()) {
            cert.getDelegate().setCertificateDownloaded(true);
            delegateRepository.save(cert.getDelegate());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Certificate-" + certCode + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

        /**
     * 3. GET /api/certificate/template?conferenceCode=PHO26
     * Retrieves the visual coordinate template for this conference.
     */
    @GetMapping("/template")
    public ResponseEntity<CertificateTemplate> getTemplate(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        return ResponseEntity.ok(certificateService.getTemplate(conferenceCode));
    }

    /**
     * 4. POST /api/certificate/template
     * Saves updated X, Y coordinate positioning and font sizes.
     */
    @PostMapping("/template")
    public ResponseEntity<CertificateTemplate> saveTemplate(@RequestBody CertificateTemplate template) {
        return ResponseEntity.ok(certificateService.saveTemplate(template));
    }
}