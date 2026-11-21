package com.joyevents.platform.controller;

import com.joyevents.platform.service.ScanningReportService;
import com.joyevents.platform.service.ScanningReportService.MatrixReportData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ScanningReportController {

    private final ScanningReportService scanningReportService;

    public ScanningReportController(ScanningReportService scanningReportService) {
        this.scanningReportService = scanningReportService;
    }

    /**
     * 1. GET /api/reports/matrix?conferenceCode=PHO26
     * Returns structured matrix data (sessions, attendees, redemption cells, totals).
     */
    @GetMapping("/matrix")
    public ResponseEntity<MatrixReportData> getMatrixReport(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {
        MatrixReportData data = scanningReportService.generateMatrixData(conferenceCode);
        return ResponseEntity.ok(data);
    }

    /**
     * 2. GET /api/reports/matrix/export?conferenceCode=PHO26
     * Streams a ready-to-use CSV matrix file for hotel catering and committee audits.
     */
    @GetMapping("/matrix/export")
    public ResponseEntity<byte[]> exportMatrixCsv(
            @RequestParam(defaultValue = "PHO26") String conferenceCode) {

        byte[] csvBytes = scanningReportService.generateMatrixCsv(conferenceCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", conferenceCode + "-scanning-matrix.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}