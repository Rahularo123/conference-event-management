package com.joyevents.platform.repository;

import com.joyevents.platform.model.ConferenceRoom;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.RoomEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomEntryRepository extends JpaRepository<RoomEntry, Long> {

    // 1. Live Headcount: Counts how many doctors entered this hall for the current lecture
    long countByRoomAndSessionName(ConferenceRoom room, String sessionName);

    // 2. Check if a doctor has already checked into this specific lecture
    boolean existsByRoomAndDelegateAndSessionName(ConferenceRoom room, Delegate delegate, String sessionName);

    // 3. CME Audit Trail: Fetch all scientific sessions attended by a specific doctor
    List<RoomEntry> findByDelegateOrderByScannedAtDesc(Delegate delegate);

    // 4. Live Door Monitor: Get the last 10 scans at this hall door
    @Query("SELECT e FROM RoomEntry e WHERE e.room = :room ORDER BY e.scannedAt DESC")
    List<RoomEntry> findRecentEntriesForRoom(@Param("room") ConferenceRoom room);
}