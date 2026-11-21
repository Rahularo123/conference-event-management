package com.joyevents.platform.repository;

import com.joyevents.platform.model.AttendanceRecord;
import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByConferenceAndDayNumber(Conference conference, int dayNumber);

    Optional<AttendanceRecord> findByConferenceAndDelegateAndDayNumber(Conference conference, Delegate delegate, int dayNumber);

    long countByConferenceAndDayNumberAndAttended(Conference conference, int dayNumber, boolean attended);

    long countByConferenceAndDayNumberAndPhotoDataIsNotNull(Conference conference, int dayNumber);
}