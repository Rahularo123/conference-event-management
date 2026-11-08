package com.joyevents.platform.repository;

import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DelegateRepository extends JpaRepository<Delegate, Long> {

    // 1. Used by Camera Scanner at Lunch & Kit counters
    Optional<Delegate> findByQrToken(String qrToken);

    // 2. Lookup by Registration ID (e.g. "PHOS-00110") within an event
    Optional<Delegate> findByConferenceAndRegistrationIdIgnoreCase(Conference conference, String registrationId);

    // 3. Duplicate check before registering a new walk-in doctor
    boolean existsByConferenceAndRegistrationId(Conference conference, String registrationId);

    // 4. List all delegates for an event in reverse order (newest first)
    List<Delegate> findByConferenceOrderByIdDesc(Conference conference);

    // 5. Omni-Search bar for Registration Desk (searches Name, RegID, Mobile, Email, or Hospital)
    @Query("SELECT d FROM Delegate d WHERE d.conference = :conf AND (" +
           "LOWER(d.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.registrationId) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.mobile) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.institution) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Delegate> searchDelegates(@Param("conf") Conference conf, @Param("q") String q);

    // 6. Live statistics for Admin Dashboard
    long countByConference(Conference conference);
    long countByConferenceAndBadgePrinted(Conference conference, boolean badgePrinted);
}