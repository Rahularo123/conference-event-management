package com.joyevents.platform.controller;

import com.joyevents.platform.model.AttendanceRecord;
import com.joyevents.platform.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 1. GET /api/attendance?conferenceCode=PHO26&dayNumber=1
     * Returns full attendance list for that day.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAttendance(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(defaultValue = "1") int dayNumber) {
        return ResponseEntity.ok(attendanceService.getAttendanceForDay(conferenceCode, dayNumber));
    }

    /**
     * 2. POST /api/attendance/quick-present?delegateId=1&dayNumber=1
     * Toggles present/absent with 1 click.
     */
    @PostMapping("/quick-present")
    public ResponseEntity<AttendanceRecord> quickPresent(
            @RequestParam Long delegateId,
            @RequestParam int dayNumber) {
        return ResponseEntity.ok(attendanceService.toggleQuickPresent(delegateId, dayNumber));
    }

    /**
     * 3. POST /api/attendance/photo
     * Saves webcam snapshot data for a doctor on a specific day.
     */
    @PostMapping("/photo")
    public ResponseEntity<AttendanceRecord> uploadLivePhoto(@RequestBody Map<String, Object> payload) {
        Long delegateId = Long.valueOf(payload.get("delegateId").toString());
        int dayNumber = Integer.parseInt(payload.get("dayNumber").toString());
        String photoData = payload.get("photoData").toString();

        return ResponseEntity.ok(attendanceService.saveLivePhoto(delegateId, dayNumber, photoData));
    }

    /**
     * 4. GET /api/attendance/stats?conferenceCode=PHO26&dayNumber=1
     * Returns HUD metrics for the active day tab.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(defaultValue = "1") int dayNumber) {
        return ResponseEntity.ok(attendanceService.getAttendanceStats(conferenceCode, dayNumber));
    }
}