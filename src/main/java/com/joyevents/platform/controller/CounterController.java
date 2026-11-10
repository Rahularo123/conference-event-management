package com.joyevents.platform.controller;

import com.joyevents.platform.service.CounterService;
import com.joyevents.platform.service.CounterService.ScanResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counter")
@CrossOrigin(origins = "*") // Allows phone cameras and tablets to talk to the API
public class CounterController {

    private final CounterService counterService;

    // Dependency Injection: Spring plugs in our CounterService automatically
    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    /**
     * POST /api/counter/redeem-lunch
     * Scans QR at Lunch counter and returns Green (Approved) or Red (Already Claimed).
     */
    @PostMapping("/redeem-lunch")
    public ResponseEntity<ScanResult> redeemLunch(
            @RequestParam String qrToken,
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(defaultValue = "1") int dayNumber,
            @RequestParam(defaultValue = "Lunch Counter 1") String counterName,
            @RequestParam(defaultValue = "staff") String operatorUsername) {

        ScanResult result = counterService.redeemLunch(qrToken, conferenceCode, dayNumber, counterName, operatorUsername);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/counter/redeem-kit
     * Scans QR at Kit desk and returns Kit entitlement balance.
     */
    @PostMapping("/redeem-kit")
    public ResponseEntity<ScanResult> redeemKit(
            @RequestParam String qrToken,
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(defaultValue = "Kit Desk") String counterName,
            @RequestParam(defaultValue = "staff") String operatorUsername) {

        ScanResult result = counterService.redeemKit(qrToken, conferenceCode, counterName, operatorUsername);
        return ResponseEntity.ok(result);
    }
}