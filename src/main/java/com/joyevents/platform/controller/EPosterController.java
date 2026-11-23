package com.joyevents.platform.controller;

import com.joyevents.platform.model.EPoster;
import com.joyevents.platform.service.EPosterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eposters")
@CrossOrigin(origins = "*")
public class EPosterController {

    private final EPosterService ePosterService;

    public EPosterController(EPosterService ePosterService) {
        this.ePosterService = ePosterService;
    }

    /**
     * 1. GET /api/eposters?conferenceCode=PHO26&query=Leukemia&track=Leukemia & Lymphoma
     * Lists e-posters with optional search query and scientific track filter.
     */
    @GetMapping
    public ResponseEntity<List<EPoster>> getPosters(
            @RequestParam(defaultValue = "PHO26") String conferenceCode,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String track) {
        return ResponseEntity.ok(ePosterService.getPosters(conferenceCode, query, track));
    }

    /**
     * 2. GET /api/eposters/tracks?conferenceCode=PHO26
     * Returns list of unique scientific tracks.
     */
    @GetMapping("/tracks")
    public ResponseEntity<List<String>> getTracks(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        return ResponseEntity.ok(ePosterService.getTracks(conferenceCode));
    }

    /**
     * 3. GET /api/eposters/{id}
     * Returns full details and abstract for a single poster.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPoster(@PathVariable Long id) {
        return ePosterService.getPosterById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Poster not found")));
    }

    /**
     * 4. POST /api/eposters/{id}/view
     * Increments view count when an attendee opens the poster.
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<EPoster> recordView(@PathVariable Long id) {
        return ResponseEntity.ok(ePosterService.recordView(id));
    }

    /**
     * 5. POST /api/eposters
     * Submits or creates an e-poster entry.
     */
    @PostMapping
    public ResponseEntity<EPoster> savePoster(@RequestBody EPoster poster) {
        return ResponseEntity.ok(ePosterService.savePoster(poster));
    }

    /**
     * 6. DELETE /api/eposters/{id}
     * Removes an e-poster.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePoster(@PathVariable Long id) {
        ePosterService.deletePoster(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "E-Poster removed"));
    }
}
