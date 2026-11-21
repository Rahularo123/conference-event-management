package com.joyevents.platform.service;

import com.joyevents.platform.model.AttendanceRecord;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.repository.AttendanceRecordRepository;
import com.joyevents.platform.repository.ConferenceRepository;
import com.joyevents.platform.repository.DelegateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ConferenceRepository conferenceRepository;
    private final DelegateRepository delegateRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                             ConferenceRepository conferenceRepository,
                             DelegateRepository delegateRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.conferenceRepository = conferenceRepository;
        this.delegateRepository = delegateRepository;
    }

    /**
     * 1. Fetches attendance records for a specific day.
     * If a doctor doesn't have a record for this day yet, it creates one (Absent by default).
     */
    @Transactional
    public List<Map<String, Object>> getAttendanceForDay(String conferenceCode, int dayNumber) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        List<Delegate> delegates = delegateRepository.findByConferenceOrderByIdDesc(conference);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Delegate delegate : delegates) {
            AttendanceRecord record = attendanceRecordRepository
                    .findByConferenceAndDelegateAndDayNumber(conference, delegate, dayNumber)
                    .orElseGet(() -> attendanceRecordRepository.save(new AttendanceRecord(conference, delegate, dayNumber, false)));

            Map<String, Object> map = new HashMap<>();
            map.put("recordId", record.getId());
            map.put("delegateId", delegate.getId());
            map.put("registrationId", delegate.getRegistrationId());
            map.put("fullName", delegate.getFullName());
            map.put("category", delegate.getCategory());
            map.put("mobile", delegate.getMobile());
            map.put("email", delegate.getEmail());
            map.put("institution", delegate.getInstitution());
            map.put("attended", record.isAttended());
            map.put("checkInTime", record.getCheckInTime());
            map.put("photoData", record.getPhotoData());
            result.add(map);
        }

        return result;
    }

    /**
     * 2. Quick Present: Toggles attendance status between Present and Absent.
     */
    @Transactional
    public AttendanceRecord toggleQuickPresent(Long delegateId, int dayNumber) {
        Delegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new IllegalArgumentException("Delegate not found: " + delegateId));

        AttendanceRecord record = attendanceRecordRepository
                .findByConferenceAndDelegateAndDayNumber(delegate.getConference(), delegate, dayNumber)
                .orElseGet(() -> new AttendanceRecord(delegate.getConference(), delegate, dayNumber, false));

        boolean newStatus = !record.isAttended();
        record.setAttended(newStatus);
        if (!newStatus) {
            record.setCheckInTime(null); // Reset check-in time if marked absent
        } else {
            record.setCheckInTime(LocalDateTime.now());
        }

        return attendanceRecordRepository.save(record);
    }

    /**
     * 3. Saves the live webcam photo captured at the counter.
     */
    @Transactional
    public AttendanceRecord saveLivePhoto(Long delegateId, int dayNumber, String base64Photo) {
        Delegate delegate = delegateRepository.findById(delegateId)
                .orElseThrow(() -> new IllegalArgumentException("Delegate not found: " + delegateId));

        AttendanceRecord record = attendanceRecordRepository
                .findByConferenceAndDelegateAndDayNumber(delegate.getConference(), delegate, dayNumber)
                .orElseGet(() -> new AttendanceRecord(delegate.getConference(), delegate, dayNumber, true));

        record.setPhotoData(base64Photo);
        record.setAttended(true); // Taking photo automatically confirms presence
        if (record.getCheckInTime() == null) {
            record.setCheckInTime(LocalDateTime.now());
        }

        return attendanceRecordRepository.save(record);
    }

    /**
     * 4. Computes live attendance metrics (Cards HUD).
     */
    public Map<String, Object> getAttendanceStats(String conferenceCode, int dayNumber) {
        Conference conference = conferenceRepository.findByCode(conferenceCode)
                .orElseThrow(() -> new IllegalArgumentException("Conference not found: " + conferenceCode));

        long totalDelegates = delegateRepository.countByConference(conference);
        long checkedIn = attendanceRecordRepository.countByConferenceAndDayNumberAndAttended(conference, dayNumber, true);
        long remaining = Math.max(0, totalDelegates - checkedIn);
        long photosClicked = attendanceRecordRepository.countByConferenceAndDayNumberAndPhotoDataIsNotNull(conference, dayNumber);

        int turnoutRate = totalDelegates > 0 ? (int) Math.round(((double) checkedIn / totalDelegates) * 100) : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDelegates", totalDelegates);
        stats.put("checkedIn", checkedIn);
        stats.put("remaining", remaining);
        stats.put("photosClicked", photosClicked);
        stats.put("turnoutRate", turnoutRate);
        stats.put("dayNumber", dayNumber);
        return stats;
    }
}