package com.joyevents.platform.repository;

import com.joyevents.platform.model.ConferenceRoom;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.RoomEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomEntryRepository extends JpaRepository<RoomEntry, Long> {

    // 1. Live Headcount: Counts ENTRY vs EXIT scans for this hall and lecture
    long countByRoomAndSessionName(ConferenceRoom room, String sessionName);

    long countByRoomAndSessionNameAndScanType(ConferenceRoom room, String sessionName, String scanType);

    // 2. Check latest door scan state (ENTRY vs EXIT) for a delegate in this lecture
    Optional<RoomEntry> findTopByRoomAndDelegateAndSessionNameOrderByScannedAtDesc(ConferenceRoom room, Delegate delegate, String sessionName);

    boolean existsByRoomAndDelegateAndSessionName(ConferenceRoom room, Delegate delegate, String sessionName);

    // 3. CME Audit Trail: Fetch all scientific sessions attended by a specific doctor
    List<RoomEntry> findByDelegateOrderByScannedAtDesc(Delegate delegate);

    // 4. Live Door Monitor: Get the last 10 scans at this hall door
    @Query("SELECT e FROM RoomEntry e WHERE e.room = :room ORDER BY e.scannedAt DESC")
    List<RoomEntry> findRecentEntriesForRoom(@Param("room") ConferenceRoom room);
}