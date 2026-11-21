package com.joyevents.platform.controller;

import com.joyevents.platform.model.PrivilegeSession;
import com.joyevents.platform.service.PrivilegeService;
import com.joyevents.platform.service.PrivilegeService.RedemptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/privileges")
@CrossOrigin(origins = "*") // Allows mobile cameras and scanning tablets to connect
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    public PrivilegeController(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    /**
     * 1. GET /api/privileges?conferenceCode=PHO26
     * Lists all meal, kit, and workshop sessions configured for this event.
     */
    @GetMapping
    public ResponseEntity<List<PrivilegeSession>> getAllSessions(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        return ResponseEntity.ok(privilegeService.getSessions(conferenceCode));
    }

    /**
     * 2. GET /api/privileges/active?conferenceCode=PHO26
     * Returns sessions that are currently open according to the live clock.
     */
    @GetMapping("/active")
    public ResponseEntity<List<PrivilegeSession>> getActiveSessions(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        return ResponseEntity.ok(privilegeService.getActiveSessionsNow(conferenceCode));
    }

    /**
     * 3. POST /api/privileges
     * Creates or updates a privilege session (e.g. Day 1 Lunch, Gala Dinner).
     */
    @PostMapping
    public ResponseEntity<PrivilegeSession> saveSession(@RequestBody PrivilegeSession session) {
        return ResponseEntity.ok(privilegeService.saveSession(session));
    }

    /**
     * 4. PUT /api/privileges/{id}/toggle
     * Enables or disables a session with one click.
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<PrivilegeSession> toggleSession(@PathVariable Long id) {
        return ResponseEntity.ok(privilegeService.toggleActive(id));
    }

    /**
     * 5. DELETE /api/privileges/{id}
     * Removes an unwanted session.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable Long id) {
        privilegeService.deleteSession(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Session removed"));
    }

    /**
     * 6. POST /api/privileges/scan
     * Real-time verification and redemption when a badge QR is scanned at a counter.
     */
    @PostMapping("/scan")
    public ResponseEntity<RedemptionResult> scanBadge(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam Long sessionId,
            @RequestParam String qrOrRegId,
            @RequestParam(defaultValue = "Counter 1") String counterName,
            @RequestParam(defaultValue = "staff") String operatorUsername) {

        RedemptionResult result = privilegeService.redeemPrivilege(
                conferenceCode, sessionId, qrOrRegId, counterName, operatorUsername
        );
        return ResponseEntity.ok(result);
    }
}