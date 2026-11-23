package com.joyevents.platform.controller;

import com.joyevents.platform.model.ConferenceRoom;
import com.joyevents.platform.service.RoomTrackingService;
import com.joyevents.platform.service.RoomTrackingService.RoomScanResult;
import com.joyevents.platform.service.RoomTrackingService.RoomStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*") // Allows door tablets and mobile scanners on venue Wi-Fi
public class RoomTrackingController {

    private final RoomTrackingService roomTrackingService;

    public RoomTrackingController(RoomTrackingService roomTrackingService) {
        this.roomTrackingService = roomTrackingService;
    }

    /**
     * 1. GET /api/rooms?conferenceCode=PHO26
     * Lists all conference halls and auditoriums configured for this event.
     */
    @GetMapping
    public ResponseEntity<List<ConferenceRoom>> getRooms(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        return ResponseEntity.ok(roomTrackingService.getRooms(conferenceCode));
    }

    /**
     * 2. GET /api/rooms/active?conferenceCode=PHO26
     * Lists active halls for door scanner dropdowns.
     */
    @GetMapping("/active")
    public ResponseEntity<List<ConferenceRoom>> getActiveRooms(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        return ResponseEntity.ok(roomTrackingService.getActiveRooms(conferenceCode));
    }

    /**
     * 3. POST /api/rooms
     * Creates or updates a conference hall (e.g. Bhargava Auditorium, Capacity 350).
     */
    @PostMapping
    public ResponseEntity<ConferenceRoom> saveRoom(@RequestBody ConferenceRoom room) {
        return ResponseEntity.ok(roomTrackingService.saveRoom(room));
    }

    /**
     * 4. PUT /api/rooms/{id}/session
     * Updates the academic presentation currently being presented in this hall.
     */
    @PutMapping("/{id}/session")
    public ResponseEntity<ConferenceRoom> updateSession(
            @PathVariable Long id,
            @RequestParam String sessionTitle) {
        return ResponseEntity.ok(roomTrackingService.updateCurrentSession(id, sessionTitle));
    }

    /**
     * 5. GET /api/rooms/{id}/status
     * Returns real-time occupancy, percentage, seat capacity, and "isFull" flag.
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<RoomStatus> getRoomStatus(@PathVariable Long id) {
        return ResponseEntity.ok(roomTrackingService.getRoomStatus(id));
    }

    /**
     * 6. POST /api/rooms/{id}/scan
     * Door scan: Validates badge, records CME presence, and increments live headcount.
     */
    @PostMapping("/{id}/scan")
    public ResponseEntity<RoomScanResult> scanIntoRoom(
            @PathVariable Long id,
            @RequestParam String qrOrRegId,
            @RequestParam(defaultValue = "ENTRY") String scanType,
            @RequestParam(defaultValue = "door_volunteer") String operatorUsername) {

        RoomScanResult result = roomTrackingService.scanIntoRoom(id, qrOrRegId, scanType, operatorUsername);
        return ResponseEntity.ok(result);
    }

    /**
     * 7. DELETE /api/rooms/{id}
     * Removes an auditorium configuration.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable Long id) {
        roomTrackingService.deleteRoom(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Room removed successfully"));
    }
}