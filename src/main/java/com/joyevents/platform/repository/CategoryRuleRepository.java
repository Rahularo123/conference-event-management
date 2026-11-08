package com.joyevents.platform.repository;

import com.joyevents.platform.model.CategoryRule;
import com.joyevents.platform.model.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {

    // Returns all category rules (Faculty, Delegate, VIP) for a given conference
    List<CategoryRule> findByConference(Conference conference);

    // Looks up the specific rule for a category name (ignoring uppercase/lowercase differences)
    Optional<CategoryRule> findByConferenceAndCategoryNameIgnoreCase(Conference conference, String categoryName);
}