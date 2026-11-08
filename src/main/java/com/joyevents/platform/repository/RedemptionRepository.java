package com.joyevents.platform.repository;

import com.joyevents.platform.model.Conference;
import com.joyevents.platform.model.Delegate;
import com.joyevents.platform.model.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {

    // Count how many items of this type (e.g. "KIT") the delegate has claimed in total
    long countByDelegateAndItemType(Delegate delegate, String itemType);

    // Count how many items the delegate claimed on a specific day (e.g. "LUNCH" on Day 1)
    long countByDelegateAndItemTypeAndDayNumber(Delegate delegate, String itemType, int dayNumber);

    // Get all redemptions for an event (newest first) for live activity feed
    List<Redemption> findByConferenceOrderByIdDesc(Conference conference);

    // Total counts for Admin Dashboard stats (e.g. Total Day 1 lunches served)
    long countByConferenceAndItemTypeAndDayNumber(Conference conference, String itemType, int dayNumber);
    long countByConferenceAndItemType(Conference conference, String itemType);
}