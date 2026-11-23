package com.joyevents.platform.repository;

import com.joyevents.platform.model.ConferenceRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferenceRoomRepository extends JpaRepository<ConferenceRoom, Long> {

    // 1. Fetch all halls configured for a specific conference
    List<ConferenceRoom> findByConferenceCodeAndActiveTrue(String conferenceCode);

    // 2. Fetch all rooms (including paused/inactive) for the admin setup table
    List<ConferenceRoom> findByConferenceCodeOrderByNameAsc(String conferenceCode);
}