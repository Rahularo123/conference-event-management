package com.joyevents.platform.service;

import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.PrivilegeSession;
import com.joyevents.platform.model.Redemption;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.PrivilegeSessionRepository;
import com.joyevents.platform.repository.RedemptionRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ScanningReportService {

    private final ConferenceRepository conferenceRepository;
    private final DelegateRepository delegateRepository;
    private final PrivilegeSessionRepository sessionRepository;
    private final RedemptionRepository redemptionRepository;

    public ScanningReportService(ConferenceRepository conferenceRepository,
                                 DelegateRepository delegateRepository,
                                 PrivilegeSessionRepository sessionRepository,
                                 RedemptionRepository redemptionRepository) {
        this.conferenceRepository = conferenceRepository;
        this.delegateRepository = delegateRepository;
        this.sessionRepository = sessionRepository;
        this.redemptionRepository = redemptionRepository;
    }

    // ── DATA TRANSFER OBJECTS (DTOs) FOR THE MATRIX ──

    public record MatrixSessionHeader(Long id, String name, String privilegeType, Integer dayNumber) {}

    public record MatrixRow(
            Long delegateId,
            String registrationId,
            String fullName,
            String category,
            String mobile,
            String institution,
            Map<Long, String> redemptions // sessionId -> "12:45 PM" (or null if not claimed)
    ) {}

    public record MatrixReportData(
            String conferenceName,
            List<MatrixSessionHeader> sessions,
            List<MatrixRow> rows,
            Map<Long, Long> sessionTotals // sessionId -> count of meals/kits served
    ) {}

    /**
     * Computes the complete cross-tabulation matrix for web display.
     */
    public MatrixReportData generateMatrixData(String conferenceCode) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        List<PrivilegeSession> sessions = sessionRepository.findByConferenceCodeOrderByDayNumberAscStartTimeAsc(conferenceCode);
        List<Delegate> delegates = delegateRepository.findByConferenceOrderByIdDesc(conference);
        List<Redemption> redemptions = redemptionRepository.findByConferenceOrderByIdDesc(conference);

        // Group redemptions by delegateId and sessionName
        // Key: "delegateId_sessionName", Value: Formatted Timestamp
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        Map<String, String> lookup = new HashMap<>();
        Map<Long, Long> sessionTotals = new HashMap<>();

        for (PrivilegeSession s : sessions) {
            sessionTotals.put(s.getId(), 0L);
        }

        for (Redemption r : redemptions) {
            if ("SUCCESS".equalsIgnoreCase(r.getStatus())) {
                String key = r.getDelegate().getId() + "_" + r.getItemType().trim().toUpperCase();
                lookup.put(key, r.getRedeemedAt().format(timeFormatter));
            }
        }

        // Build Rows
        List<MatrixRow> rows = new ArrayList<>();
        for (Delegate d : delegates) {
            Map<Long, String> delegateRedemptions = new HashMap<>();
            for (PrivilegeSession s : sessions) {
                String key = d.getId() + "_" + s.getName().trim().toUpperCase();
                if (lookup.containsKey(key)) {
                    String time = lookup.get(key);
                    delegateRedemptions.put(s.getId(), time);
                    sessionTotals.put(s.getId(), sessionTotals.get(s.getId()) + 1);
                } else {
                    delegateRedemptions.put(s.getId(), null);
                }
            }
            rows.add(new MatrixRow(
                    d.getId(),
                    d.getRegistrationId(),
                    d.getFullName(),
                    d.getCategory(),
                    d.getMobile() != null ? d.getMobile() : "",
                    d.getInstitution() != null ? d.getInstitution() : "",
                    delegateRedemptions
            ));
        }

        List<MatrixSessionHeader> headers = sessions.stream()
                .map(s -> new MatrixSessionHeader(s.getId(), s.getName(), s.getPrivilegeType(), s.getDayNumber()))
                .toList();

        return new MatrixReportData(conference.getName(), headers, rows, sessionTotals);
    }

    /**
     * Generates a downloadable CSV matrix file for hotel/caterer billing.
     */
    public byte[] generateMatrixCsv(String conferenceCode) {
        MatrixReportData data = generateMatrixData(conferenceCode);
        StringBuilder sb = new StringBuilder();

        // 1. CSV Header Row
        sb.append("Reg ID,Doctor Name,Category,Mobile,Institution");
        for (MatrixSessionHeader s : data.sessions()) {
            sb.append(",\"").append(s.name()).append("\"");
        }
        sb.append("\n");

        // 2. Attendee Rows
        for (MatrixRow r : data.rows()) {
            sb.append("\"").append(r.registrationId()).append("\",")
              .append("\"").append(r.fullName()).append("\",")
              .append("\"").append(r.category()).append("\",")
              .append("\"").append(r.mobile()).append("\",")
              .append("\"").append(r.institution()).append("\"");

            for (MatrixSessionHeader s : data.sessions()) {
                String time = r.redemptions().get(s.id());
                if (time != null) {
                    sb.append(",\"YES (").append(time).append(")\"");
                } else {
                    sb.append(",\"NO\"");
                }
            }
            sb.append("\n");
        }

        // 3. Totals Summary Row at the bottom
        sb.append("\"TOTALS\",,\"\",\"\",");
        for (MatrixSessionHeader s : data.sessions()) {
            sb.append(",\"").append(data.sessionTotals().get(s.id())).append(" Served\"");
        }
        sb.append("\n");

        return sb.toString().getBytes();
    }
}