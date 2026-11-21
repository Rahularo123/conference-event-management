package com.joyevents.platform.repository;

import com.joyevents.platform.model.PrivilegeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrivilegeSessionRepository extends JpaRepository<PrivilegeSession, Long> {

    // 1. Fetch all privilege sessions for a conference ordered by day and timing
    List<PrivilegeSession> findByConferenceCodeOrderByDayNumberAscStartTimeAsc(String conferenceCode);

    // 2. Fetch sessions filtered by a specific day (Day 1, Day 2, Day 3)
    List<PrivilegeSession> findByConferenceCodeAndDayNumber(String conferenceCode, Integer dayNumber);

    // 3. Fetch currently open sessions based on live clock time
    @Query("SELECT s FROM PrivilegeSession s WHERE s.conferenceCode = :confCode AND s.active = true AND :currentTime BETWEEN s.startTime AND s.endTime")
    List<PrivilegeSession> findActiveSessionsNow(@Param("confCode") String confCode, @Param("currentTime") LocalDateTime currentTime);

    // 4. Fetch all active sessions regardless of current time (for desk dropdown selection)
    List<PrivilegeSession> findByConferenceCodeAndActiveTrue(String conferenceCode);
}