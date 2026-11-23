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

    public RoomStatus getRoomStatus(Long roomId) {
        ConferenceRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        String currentSession = room.getCurrentSession() != null ? room.getCurrentSession() : "General Session";
        long occupancy = entryRepository.countByRoomAndSessionName(room, currentSession);
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

        // 2. Check for duplicate entry into the same lecture
        boolean alreadyScanned = entryRepository.existsByRoomAndDelegateAndSessionName(room, delegate, sessionName);

        // 3. Current Headcount check
        long currentCount = entryRepository.countByRoomAndSessionName(room, sessionName);
        if (currentCount >= room.getCapacity() && !alreadyScanned) {
            return new RoomScanResult(false, "HALL FULL: Seating capacity of " + room.getCapacity() + " reached!", 
                    delegate.getFullName(), delegate.getRegistrationId(), room.getName(), sessionName, currentCount, room.getCapacity(), now());
        }

        // 4. Record new entry log
        if (!alreadyScanned) {
            RoomEntry entry = new RoomEntry(room, delegate, sessionName, scanType != null ? scanType : "ENTRY", operatorUsername);
            entryRepository.save(entry);
            currentCount++;
        }

        String statusMsg = alreadyScanned ? "RE-ENTRY VERIFIED: Already logged for this session" : "ENTRY APPROVED: CME presence recorded";

        return new RoomScanResult(true, statusMsg, delegate.getFullName(), delegate.getRegistrationId(), 
                room.getName(), sessionName, currentCount, room.getCapacity(), now());
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }
}