package com.joyevents.platform.service;

import com.joyevents.platform.model.ConferenceRoom;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.RoomEntry;
import com.joyevents.platform.repository.ConferenceRoomRepository;
import com.joyevents.platform.repository.DelegateRepository;
import com.joyevents.platform.repository.RoomEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class RoomTrackingService {

    private final ConferenceRoomRepository roomRepository;
    private final RoomEntryRepository entryRepository;
    private final DelegateRepository delegateRepository;

    public RoomTrackingService(ConferenceRoomRepository roomRepository,
                               RoomEntryRepository entryRepository,
                               DelegateRepository delegateRepository) {
        this.roomRepository = roomRepository;
        this.entryRepository = entryRepository;
        this.delegateRepository = delegateRepository;
    }

    // ── DATA TRANSFER OBJECTS (DTOs) ──

    public record RoomStatus(
            Long roomId,
            String roomName,
            int capacity,
            long currentOccupancy,
            int occupancyPercentage,
            String currentSession,
            boolean isFull
    ) {}

    public record RoomScanResult(
            boolean success,
            String message,
            String delegateName,
            String registrationId,
            String roomName,
            String sessionName,
            long currentOccupancy,
            int capacity,
            String timestamp
    ) {}

    // ── 1. ROOM SETUP & CONFIGURATION (room-setup.php) ──

    public List<ConferenceRoom> getRooms(String conferenceCode) {
        return roomRepository.findByConferenceCodeOrderByNameAsc(conferenceCode);
    }

    public List<ConferenceRoom> getActiveRooms(String conferenceCode) {
        return roomRepository.findByConferenceCodeAndActiveTrue(conferenceCode);
    }

    @Transactional
    public ConferenceRoom saveRoom(ConferenceRoom room) {
        return roomRepository.save(room);
    }

    @Transactional
    public ConferenceRoom updateCurrentSession(Long roomId, String sessionTitle) {
        ConferenceRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        room.setCurrentSession(sessionTitle);
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        roomRepository.deleteById(roomId);
    }

    // ── 2. LIVE OCCUPANCY STATUS ──

    private long computeActiveOccupancy(ConferenceRoom room, String sessionName) {
        long entries = entryRepository.countByRoomAndSessionNameAndScanType(room, sessionName, "ENTRY");
        long exits = entryRepository.countByRoomAndSessionNameAndScanType(room, sessionName, "EXIT");
        return Math.max(0, entries - exits);
    }

    public RoomStatus getRoomStatus(Long roomId) {
        ConferenceRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        String currentSession = room.getCurrentSession() != null ? room.getCurrentSession() : "General Session";
        long occupancy = computeActiveOccupancy(room, currentSession);
        int percentage = room.getCapacity() > 0 ? (int) Math.min(100, (occupancy * 100) / room.getCapacity()) : 0;
        boolean isFull = occupancy >= room.getCapacity();

        return new RoomStatus(room.getId(), room.getName(), room.getCapacity(), occupancy, percentage, currentSession, isFull);
    }

    // ── 3. DOOR SCAN & CME TRACKING (roomEntry.php) ──

    @Transactional
    public RoomScanResult scanIntoRoom(Long roomId, String qrOrRegId, String scanType, String operatorUsername) {
        ConferenceRoom room = roomRepository.findById(roomId)
                .orElse(null);
        if (room == null || !room.isActive()) {
            return new RoomScanResult(false, "HALL CLOSED: This room is currently not accepting entries.", 
                    null, null, null, null, 0, 0, now());
        }

        // 1. Lookup Doctor by QR or Registration ID
        Optional<Delegate> delegateOpt = delegateRepository.findByQrToken(qrOrRegId);
        if (delegateOpt.isEmpty()) {
            delegateOpt = delegateRepository.findByRegistrationIdIgnoreCase(qrOrRegId);
        }
        if (delegateOpt.isEmpty()) {
            return new RoomScanResult(false, "INVALID BADGE: No attendee found.", 
                    null, null, room.getName(), room.getCurrentSession(), 0, room.getCapacity(), now());
        }

        Delegate delegate = delegateOpt.get();
        String sessionName = room.getCurrentSession() != null ? room.getCurrentSession() : "General Session";
        String normalizedType = "EXIT".equalsIgnoreCase(scanType) ? "EXIT" : "ENTRY";

        // 2. Determine whether delegate is currently inside this session
        Optional<RoomEntry> latestOpt = entryRepository.findTopByRoomAndDelegateAndSessionNameOrderByScannedAtDesc(room, delegate, sessionName);
        boolean currentlyInside = latestOpt.isPresent() && !"EXIT".equalsIgnoreCase(latestOpt.get().getScanType());
        long currentCount = computeActiveOccupancy(room, sessionName);

        if ("EXIT".equals(normalizedType)) {
            if (!currentlyInside) {
                return new RoomScanResult(false, "NOT INSIDE HALL: Attendee is not checked in or has already left.",
                        delegate.getFullName(), delegate.getRegistrationId(), room.getName(), sessionName, currentCount, room.getCapacity(), now());
            }
            entryRepository.save(new RoomEntry(room, delegate, sessionName, "EXIT", operatorUsername));
            currentCount = Math.max(0, currentCount - 1);
            return new RoomScanResult(true, "EXIT RECORDED: Attendee left hall",
                    delegate.getFullName(), delegate.getRegistrationId(), room.getName(), sessionName, currentCount, room.getCapacity(), now());
        }

        // ENTRY flow
        if (currentlyInside) {
            return new RoomScanResult(true, "RE-ENTRY VERIFIED: Already checked into this session",
                    delegate.getFullName(), delegate.getRegistrationId(), room.getName(), sessionName, currentCount, room.getCapacity(), now());
        }

        if (currentCount >= room.getCapacity()) {
            return new RoomScanResult(false, "HALL FULL: Seating capacity of " + room.getCapacity() + " reached!", 
                    delegate.getFullName(), delegate.getRegistrationId(), room.getName(), sessionName, currentCount, room.getCapacity(), now());
        }

        entryRepository.save(new RoomEntry(room, delegate, sessionName, "ENTRY", operatorUsername));
        currentCount++;

        return new RoomScanResult(true, "ENTRY APPROVED: CME presence recorded", delegate.getFullName(), delegate.getRegistrationId(), 
                room.getName(), sessionName, currentCount, room.getCapacity(), now());
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }
}