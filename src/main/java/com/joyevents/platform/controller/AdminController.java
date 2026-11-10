package com.joyevents.platform.controller;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.Checkpoint;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.Redemption;
import com.joyevents.platform.repository.CategoryRuleRepository;
import com.joyevents.platform.repository.CheckpointRepository;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.RedemptionRepository;
import com.joyevents.platform.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ConferenceRepository conferenceRepository;
    private final DelegateRepository delegateRepository;
    private final CategoryRuleRepository categoryRuleRepository;
    private final RedemptionRepository redemptionRepository;
    private final CheckpointRepository checkpointRepository;
    private final UserRepository userRepository;

    // Dependency Injection: Spring plugs in all repositories automatically
    public AdminController(ConferenceRepository conferenceRepository,
                           DelegateRepository delegateRepository,
                           CategoryRuleRepository categoryRuleRepository,
                           RedemptionRepository redemptionRepository,
                           CheckpointRepository checkpointRepository,
                           UserRepository userRepository) {
        this.conferenceRepository = conferenceRepository;
        this.delegateRepository = delegateRepository;
        this.categoryRuleRepository = categoryRuleRepository;
        this.redemptionRepository = redemptionRepository;
        this.checkpointRepository = checkpointRepository;
        this.userRepository = userRepository;
    }
    
        @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {

        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            var u = userOpt.get();
            if (u.getPassword().equals(password) && u.isActive()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "username", u.getUsername(),
                        "fullName", u.getFullName(),
                        "role", u.getRole()
                ));
            }
        }

        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Invalid email or password!"
        ));
    }

    /**
     * 1. GET /api/admin/stats?conferenceCode=PHO26&dayNumber=1
     * Computes live statistics for dashboard cards.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(defaultValue = "1") int dayNumber) {

        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        long totalDelegates = delegateRepository.countByConference(conference);
        long badgesPrinted = delegateRepository.countByConferenceAndBadgePrinted(conference, true);
        long badgesPending = totalDelegates - badgesPrinted;
        long lunchesServed = redemptionRepository.countByConferenceAndItemTypeAndDayNumber(conference, "LUNCH", dayNumber);
        long kitsIssued = redemptionRepository.countByConferenceAndItemType(conference, "KIT");

        List<Redemption> recentScans = redemptionRepository.findByConferenceOrderByIdDesc(conference);
        if (recentScans.size() > 10) {
            recentScans = recentScans.subList(0, 10);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("conferenceName", conference.getName());
        stats.put("totalDelegates", totalDelegates);
        stats.put("badgesPrinted", badgesPrinted);
        stats.put("badgesPending", badgesPending);
        stats.put("lunchesServedToday", lunchesServed);
        stats.put("kitsIssued", kitsIssued);
        stats.put("recentScans", recentScans);

        return ResponseEntity.ok(stats);
    }

    /**
     * 2. DYNAMIC CHECKPOINTS APIs (For Mobile Scanner Dropdown & Admin Setup)
     */
    @GetMapping("/checkpoints")
    public ResponseEntity<List<Checkpoint>> getCheckpoints(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));
        return ResponseEntity.ok(checkpointRepository.findByConferenceAndActiveTrue(conference));
    }

    @PostMapping("/checkpoints")
    public ResponseEntity<Checkpoint> createCheckpoint(
            @RequestParam String conferenceCode,
            @RequestParam String name,
            @RequestParam(defaultValue = "FOOD") String type,
            @RequestParam(defaultValue = "1") int dayNumber) {

        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        Checkpoint checkpoint = new Checkpoint(conference, name, type.toUpperCase(), dayNumber);
        return ResponseEntity.ok(checkpointRepository.save(checkpoint));
    }

    @DeleteMapping("/checkpoints/{id}")
    public ResponseEntity<Map<String, Object>> deleteCheckpoint(@PathVariable Long id) {
        checkpointRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Checkpoint deleted"));
    }

    /**
     * 3. GET & POST /api/admin/conferences
     */
    @GetMapping("/conferences")
    public ResponseEntity<List<Conference>> getAllConferences() {
        return ResponseEntity.ok(conferenceRepository.findAll());
    }

    @PostMapping("/conferences")
    public ResponseEntity<Conference> createConference(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String venue,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        Conference conference = new Conference(code.toUpperCase(), name, venue,
                LocalDate.parse(startDate), LocalDate.parse(endDate));
        return ResponseEntity.ok(conferenceRepository.save(conference));
    }

    /**
     * 4. GET & POST /api/admin/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryRule>> getCategories(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));
        return ResponseEntity.ok(categoryRuleRepository.findByConference(conference));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryRule> setCategoryRule(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam String categoryName,
            @RequestParam int kitsAllowed,
            @RequestParam int lunchesAllowed,
            @RequestParam(defaultValue = "true") boolean certificateEligible) {

        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));

        CategoryRule rule = categoryRuleRepository.findByConferenceAndCategoryNameIgnoreCase(conference, categoryName)
                .orElse(new CategoryRule(conference, categoryName, kitsAllowed, lunchesAllowed, certificateEligible));

        rule.setKitsAllowed(kitsAllowed);
        rule.setLunchesAllowed(lunchesAllowed);
        rule.setCertificateEligible(certificateEligible);

        return ResponseEntity.ok(categoryRuleRepository.save(rule));
    }

    /**
     * 5. GET /api/admin/export-csv?conferenceCode=PHO26
     */
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportDelegatesCsv(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {

        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found"));

        List<Delegate> delegates = delegateRepository.findByConferenceOrderByIdDesc(conference);

        StringBuilder csv = new StringBuilder();
        csv.append("Reg ID,Full Name,Category,Email,Mobile,Institution,City,Badge Printed\n");

        for (Delegate d : delegates) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    d.getRegistrationId(),
                    d.getFullName(),
                    d.getCategory(),
                    d.getEmail() != null ? d.getEmail() : "",
                    d.getMobile() != null ? d.getMobile() : "",
                    d.getInstitution() != null ? d.getInstitution() : "",
                    d.getCity() != null ? d.getCity() : "",
                    d.isBadgePrinted() ? "YES" : "NO"));
        }

        byte[] csvBytes = csv.toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", conferenceCode + "-delegates.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}